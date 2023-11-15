/** 
 * Copyright 2019-2023, Tomasz Żebrowski
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package org.obd.metrics.api;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.obd.metrics.alert.Alerts;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Init.Header;
import org.obd.metrics.api.model.Lifecycle;
import org.obd.metrics.api.model.Lifecycle.Subscription;
import org.obd.metrics.api.model.Pids;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.buffer.decoder.ConnectorResponseBuffer;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.command.ATCommand;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.DelayCommand;
import org.obd.metrics.command.process.InitCompletedCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.context.Context;
import org.obd.metrics.diagnostic.Diagnostics;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.AdapterConnection;
import org.obd.metrics.transport.Connector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DefaultWorkflow implements Workflow {

	private transient Future<?> tasks;

	@Getter
	private Diagnostics diagnostics = Diagnostics.instance();

	@Getter
	private Alerts alerts = Alerts.instance();

	private ReplyObserver<Reply<?>> externalEventsObserver;
	private final List<Lifecycle> lifecycle;
	private final FormulaEvaluatorConfig formulaEvaluatorConfig;

	// just a single thread in a pool
	private static final ExecutorService singleTaskPool = new ThreadPoolExecutor(1, 1, 1L, TimeUnit.SECONDS,
			new SynchronousQueue<>());

	protected DefaultWorkflow(Pids pids, FormulaEvaluatorConfig formulaEvaluatorConfig,
			ReplyObserver<Reply<?>> eventsObserver, List<Lifecycle> lifecycle) {
		
		log.info("Creating an instance of the Workflow task.");
		this.formulaEvaluatorConfig = formulaEvaluatorConfig;
		this.externalEventsObserver = eventsObserver;
		this.lifecycle = lifecycle;
		updatePidRegistry(pids);
	}

	@Override
	public void updatePidRegistry(Pids pids) {
		Context.apply(it -> {
			it.register(PidDefinitionRegistry.class, buildPidDefinitionRegistry(pids));
		});
	}

	@Override
	public PidDefinitionRegistry getPidRegistry() {
		return Context.instance().forceResolve(PidDefinitionRegistry.class);
	}

	@Override
	public boolean isRunning() {
		if (tasks == null) {
			if (log.isTraceEnabled()) {
				log.trace("No workflow process is activly running.");
			}
			return false;
		} else {
			final boolean running = !tasks.isDone();
			if (log.isTraceEnabled()) {
				log.trace("Workflow process is activly running: {}", running);
			}
			return running;
		}
	}

	@Override
	public void stop(boolean gracefulStop) {

		Context.apply(context -> {
			context.resolve(Subscription.class).apply(subscription -> {
				log.info("Stopping workflow process...");
				log.info("Publishing onStopping event to let components complete.");

				subscription.onStopping();

				if (!gracefulStop) {
					context.resolve(Connector.class).apply(connector -> {
						try {
							log.info("Graceful stop is not enabled. Closing streams by force.");
							connector.close();
						} catch (Exception e) {
							subscription.onError("Failed to add close connector", e);
						}
					});

				}

				log.info("Stopping the Workflow task.");

				context.resolve(CommandsBuffer.class).apply(commandsBuffer -> {
					try {
						log.debug("Publishing QUIT command...");
						commandsBuffer.addFirst(new QuitCommand());
					} catch (Exception e) {
						subscription.onError("Failed to add quite command", e);
					}

					try {
						log.debug("Deleting existing commands from the CommandsBuffer.");
						commandsBuffer.clear();
					} catch (Exception e) {
						subscription.onError("Failed to clear buffer", e);
					}
				});

				context.resolve(ConnectorResponseBuffer.class).apply(commandsBuffer -> {
					try {
						log.debug("Deleting existing commands from the ConnectorResponseBuffer.");
						commandsBuffer.clear();
					} catch (Exception e) {
						subscription.onError("Failed to clear buffer", e);
					}
				});
			});
		});

		if (tasks == null) {
			log.error("No workflow is currently running, nothing to stop");
		} else {
			tasks.cancel(true);
		}
	}

	@Override
	public WorkflowExecutionStatus updateQuery(@NonNull Query query, @NonNull Init init,
			@NonNull Adjustments adjustments) {
		
		log.info("Updating running workflow with new query:{} and init settings: {} ", query.getPids(), init.getHeaders());
		
		debugPIDs(query, init, adjustments);
		
		if (isRunning()) {
			Context.apply(it -> {

				final CommandProducer commandProducer = it.forceResolve(CommandProducer.class);

				log.info("Workflow is already running. Pausing command producer");
				diagnostics.rate().reset();
				commandProducer.pause();

				it.forceResolve(CommandsBuffer.class).clear();

				commandProducer.updateSettings(adjustments, getCommandsSupplier(init, adjustments, query), diagnostics,
						init);

				log.info("Resuming command producer");
				commandProducer.resume();
			});
			return WorkflowExecutionStatus.UPDATED;
		} else {
			log.warn("No workflow is running");
		}

		return WorkflowExecutionStatus.NOT_RUNNING;
	}

	@Override
	public WorkflowExecutionStatus start(@NonNull AdapterConnection connection, @NonNull Query query,
			@NonNull Init init, @NonNull Adjustments adjustments) {

		final Runnable task = () -> {
			final ExecutorService executorService = Executors.newFixedThreadPool(3);

			try {

				log.info("Starting the Workflow task.\n Protocol: {}, headers: {},DBEUG: {},selected PID's: {}, adjustements: {}",
						init.getProtocol(), init.getHeaders(), adjustments.isDebugEnabled(), query.getPids(),
						adjustments);
				
				debugPIDs(query, init, adjustments);
				
				final ConnectionManager connectionManager = new ConnectionManager(connection, adjustments);

				Context.apply(it -> {
					final PidDefinitionRegistry pidDefinitionRegistry = it.forceResolve(PidDefinitionRegistry.class);

					it.reset();
					it.register(PidDefinitionRegistry.class, pidDefinitionRegistry);
					it.register(Subscription.class, new Subscription()).apply(p -> {
						lifecycle.forEach(l -> {
							p.subscribe(l);
						});
					});
					it.register(ConnectorResponseBuffer.class, ConnectorResponseBuffer.instance());
					it.register(CodecRegistry.class, CodecRegistry.builder()
							.formulaEvaluatorConfig(formulaEvaluatorConfig).adjustments(adjustments).build());
					it.register(ConnectionManager.class, connectionManager);
					prepareInitBuffer(init, adjustments, it);
				});

				final CommandProducer commandProducerThread = buildCommandProducer(adjustments,
						getCommandsSupplier(init, adjustments, query), init);
				final CommandLoop commandLoopThread = new CommandLoop();
				final ConnectorResponseDecoder connectorResponseDecoderThread = new ConnectorResponseDecoder(
						adjustments);

				Context.apply(it -> {
					it.resolve(Subscription.class).apply(p -> {
						p.subscribe(connectorResponseDecoderThread);
						p.subscribe(commandProducerThread);
						p.subscribe(commandLoopThread);
						p.subscribe(connectionManager);
						p.onConnecting();
					});

					it.register(CommandProducer.class, commandProducerThread);

					it.register(EventsPublishlisher.class,
							EventsPublishlisher.builder().observer(externalEventsObserver)
									.observer((ReplyObserver<Reply<?>>) alerts)
									.observer((ReplyObserver<Reply<?>>) diagnostics).build());

					it.init();
				});

				alerts.reset();
				diagnostics.reset();

				executorService.invokeAll(
						Arrays.asList(commandLoopThread, commandProducerThread, connectorResponseDecoderThread));

			} catch (InterruptedException e) {
				log.info("Process was interupted.");
			} catch (Throwable e) {
				log.error("Failed to initialize the Workflow task.", e);
			} finally {
				try {
					log.info("Stopping the Workflow task.");

					notifyStopped();

					executorService.shutdown();
				} catch (Throwable e) {
					log.error("Error occured while stopping the workflow.", e);
				}
			}
		};

		try {
			log.info("Submitting the Workflow task.");
			tasks = singleTaskPool.submit(task);
			return WorkflowExecutionStatus.STARTED;

		} catch (RejectedExecutionException e) {
			log.warn("Workflow task was rejected. There is already running task in the queue");
		}

		return WorkflowExecutionStatus.REJECTED;
	}

	private void debugPIDs(Query query, Init init, Adjustments adjustments) {

		final PidDefinitionRegistry pidDefinitionRegistry = Context.instance()
				.forceResolve(PidDefinitionRegistry.class);
		final Map<String, Header> canHeaders = init.getHeaders().stream()
				.collect(Collectors.toMap(Header::getMode, Function.identity()));

		query.getPids().forEach(id -> {
			final PidDefinition pid = pidDefinitionRegistry.findBy(id);

			if (pid == null) {
				log.error("There is no PID for id={}",id);
			}else {
				if (adjustments.isDebugEnabled()) {
					final ObjectMapper objMapper = new ObjectMapper();
					try {
						String serialized = objMapper.writeValueAsString(pid);
						log.info("Available PID=[{}:{}] in the registry \n{}",id, pid.getPid(),  serialized);
					} catch (JsonProcessingException e) {
						log.warn("Failed to serialize PID to string");
					}
				}
				
				String mode = pid.getMode();
				final boolean hasOverrides = pid.getOverrides().getCanMode() != null && pid.getOverrides().getCanMode().length() > 0;
				if (hasOverrides) {
					mode = pid.getOverrides().getCanMode();
				}
				String header = "";
				if (canHeaders.containsKey(mode)) {
					header = canHeaders.get(mode).getHeader();
				}
	
				log.info("Mapping for a PID=[{}:{}] is: mode={}, header={}, hasOverrides={}", id, pid.getPid(), mode, header, hasOverrides);
			}
		});
	}

	private void notifyStopped() {
		log.info("Notyfing workflow is stopped");
		Context.instance().resolve(Subscription.class).apply(p -> {
			p.onStopped();
		});
	}

	private void prepareInitBuffer(Init init, Adjustments adjustements, Context it) {
		it.register(CommandsBuffer.class, CommandsBuffer.instance()).apply(commandsBuffer -> {
			commandsBuffer.clear();
			init.getSequence().getCommands().stream().forEach(c -> {
				if (c instanceof DelayCommand) {
					log.info("Setting delay after ATZ command: {}", init.getDelayAfterReset());
					((DelayCommand) c).setDelay(init.getDelayAfterReset());
				}
			});
			commandsBuffer.add(init.getSequence());

			// Protocol
			commandsBuffer.addLast(new ATCommand("SP" + init.getProtocol().getType()));
			PIDsGroupHandler.appendBuffer(init, adjustements);
			commandsBuffer.addLast(new DelayCommand(init.getDelayAfterInit()));
			commandsBuffer.addLast(new InitCompletedCommand());
		});
	}

	private CommandProducer buildCommandProducer(Adjustments adjustements, Supplier<List<ObdCommand>> supplier,
			Init init) {
		return new CommandProducer(diagnostics, supplier, adjustements, init);
	}

	private PidDefinitionRegistry buildPidDefinitionRegistry(Pids pids) {
		long tt = System.currentTimeMillis();
		PidDefinitionRegistry pidRegistry = null;
		try (final Resources sources = Resources.convert(pids)) {
			pidRegistry = PidDefinitionRegistry.builder().sources(sources.getResources()).build();
		}
		tt = System.currentTimeMillis() - tt;
		log.info("Loading resources files took: {}ms.", tt);
		return pidRegistry;
	}

	private Supplier<List<ObdCommand>> getCommandsSupplier(Init init, Adjustments adjustements, Query query) {
		return new CommandsSuplier(getPidRegistry(), adjustements, query, init);
	}

}
