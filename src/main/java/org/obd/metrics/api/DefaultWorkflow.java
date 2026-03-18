 /**
 * Copyright 2019-2026, Tomasz Żebrowski
 *
 * <p>Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.obd.metrics.api;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.obd.metrics.alert.Alerts;
import org.obd.metrics.api.WorkflowOrchestrator.Task;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.DtcAction;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Init.Header;
import org.obd.metrics.api.model.Lifecycle;
import org.obd.metrics.api.model.Lifecycle.Subscription;
import org.obd.metrics.api.model.Pids;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.api.model.SniffingPolicy;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.buffer.decoder.ConnectorResponseBuffer;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.command.ATCommand;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.DelayCommand;
import org.obd.metrics.command.process.DiagnosticTroubleCodeScheduleCommand;
import org.obd.metrics.command.process.InitCompletedCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.command.routine.RoutineCommand;
import org.obd.metrics.diagnostic.Diagnostics;
import org.obd.metrics.pid.PIDsGroup;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.AdapterConnection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DefaultWorkflow implements Workflow {

	@Getter
	private Diagnostics diagnostics = Diagnostics.instance();

	@Getter
	private Alerts alerts = Alerts.instance();

	private ReplyObserver<Reply<?>> externalEventsObserver;
	private final List<Lifecycle> lifecycle;
	private final FormulaEvaluatorConfig formulaEvaluatorConfig;

	private CommandProducer commandProducer;
	private CommandsBuffer commandsBuffer = CommandsBuffer.instance();
	private ConnectorResponseBuffer connectoreResponseBuffer = ConnectorResponseBuffer.instance();
	private PidDefinitionRegistry registry;
	private final Subscription subscription = new Subscription();
	private ConnectionManager connectionManager;
	
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
		this.registry = buildPidDefinitionRegistry(pids);
	}

	@Override
	public PidDefinitionRegistry getPidRegistry() {
		return registry;
	}

	@Override
	public boolean isRunning() {
		return WorkflowOrchestrator.instance().isRunning(this);
	}

	@Override
	public void stop(boolean gracefulStop) {
		
		log.info("Stopping workflow process...");
		log.info("Publishing onStopping event to let components complete.");

		subscription.onStopping();

		if (!gracefulStop) {
			try {
				log.info("Graceful stop is not enabled. Closing streams by force.");
				connectionManager.getConnector().close();
			} catch (Exception e) {
				subscription.onError("Failed to add close connector", e);
			}
		
		}

		log.info("Stopping the Workflow task.");

		try {
			log.debug("Deleting existing commands from the CommandsBuffer.");
			commandsBuffer.clear();
		} catch (Exception e) {
			subscription.onError("Failed to clear buffer", e);
		}
		
		
		try {
			log.debug("Deleting existing commands from the ConnectorResponseBuffer.");
			connectoreResponseBuffer.clear();
		} catch (Exception e) {
			subscription.onError("Failed to clear buffer", e);
		}
	
		try {
			log.debug("Publishing QUIT command...");
			commandsBuffer.addFirst(new QuitCommand());
		} catch (Exception e) {
			subscription.onError("Failed to add quite command", e);
		}


		subscription.clear();
			
		
		WorkflowOrchestrator.instance().stop(this);
	}

	@Override
	public WorkflowExecutionStatus scheduleDTCAction(final Set<DtcAction> actions) {
		log.info("[DTC] Scheduling DTC action: {}", actions);
		
		if (isRunning()) {
			
			
			log.info("[DTC] Workflow is already running. Pausing command producer: {}", commandProducer);

			commandProducer.pause();
			commandsBuffer.clear();

			final PidDefinitionRegistry registry = getPidRegistry();
			if (actions.contains(DtcAction.CLEAR)) {
				registry.findBy(PIDsGroup.DTC_CLEAR).forEach(c -> {
					log.info("[DTC] Adding DTC clear command {}", c);
					commandsBuffer.addLast(new ObdCommand(c));
				});
			}

			if (actions.contains(DtcAction.READ) || actions.contains(DtcAction.READ_SNAPSHPOTS)) {
				registry.findBy(PIDsGroup.DTC_READ).forEach(c -> {
					log.info("[DTC] Adding DTC read command {}", c);
					commandsBuffer.addLast(new ObdCommand(c));
				});
			}

			log.info("[DTC] Adding DTC schedule command");
			commandsBuffer.addLast(new DiagnosticTroubleCodeScheduleCommand(actions));
			commandProducer.resume();

			return WorkflowExecutionStatus.DTC_QUEUED;
		} else {
			log.warn("[DTC] No workflow is running.");
			return WorkflowExecutionStatus.NOT_RUNNING;
		}
		
	}

	@Override
	public WorkflowExecutionStatus executeRoutine(@NonNull Long routineId, @NonNull Init init) {

		log.info("[Routine] Executing routine");
		log.info("[Routine] Selected routine: {}", routineId);
		log.info("[Routine] Protocol: {}, headers: {}", init.getProtocol(), init.getHeaders());
		
		if (isRunning()) {
			log.info("[Routine] Workflow is already running. Pausing command producer");

			commandProducer.pause();

			final PidDefinition pid = getPidRegistry().findBy(routineId);

			if (pid == null) {
				log.info("[Routine] No routine found for given ID={}", routineId);
				return WorkflowExecutionStatus.REJECTED;
			} else {
				if (PIDsGroup.ROUTINE.equals(pid.getGroup())) {
					// can request id
					init.getHeaders().stream().filter(w -> w.getMode().equals(pid.deductMode())).findFirst()
							.ifPresent(id -> commandsBuffer.addLast(new ATCommand("SH" + id.getHeader())));

					// extended diagnosis session
					commandsBuffer.addLast(UDSConstants.UDS_EXTENDED_SESSION);
					// tester availability
					commandsBuffer.addLast(UDSConstants.UDS_TESTER_AVAILIBILITY);

					// routine
					commandsBuffer.addLast(new RoutineCommand(pid));

					// default diagnosis session
					commandsBuffer.addLast(UDSConstants.UDS_DEFAULT_SESSION);
				} else {
					log.info("[Routine] Given ID={} is not routine type", routineId);
					return WorkflowExecutionStatus.REJECTED;
				}
			}

			final Adjustments adjustments = Adjustments.DEFAULT;
			log.info("[Routine] Removing cyclic commands from command producer.");
			commandProducer.updateSettings(adjustments, getCommandsSupplier(init, adjustments, Query.builder().build()),
					diagnostics, init);

			commandProducer.resume();


			return WorkflowExecutionStatus.ROUTINE_QUEUED;
		} else {
			log.warn("[Routine] No workflow is running");
			return WorkflowExecutionStatus.NOT_RUNNING;
		}
	}

	@Override
	public WorkflowExecutionStatus updateQuery(@NonNull Query query, @NonNull Init init,
			@NonNull Adjustments adjustments) {

	
		long ts = System.currentTimeMillis();

		log.info("[Update] Updating running workflow with new query");
		log.info("[Update] Selected PID's: {}", query.getPids());
		log.info("[Update] Protocol: {}, headers: {}", init.getProtocol(), init.getHeaders());
		log.info("[Update] Debug: {}", adjustments.isDebugEnabled());
		log.info("[Update] Batch policy: {}", adjustments.getBatchPolicy());
		log.info("[Update] Stn exetnsion: {}", adjustments.getStNxx());
		
	
		if (isRunning()) {
			debugPIDs(query, init, adjustments);
			
			log.info("Workflow is already running. Pausing command producer");
			diagnostics.rate().reset();
			commandProducer.pause();

			commandsBuffer.clear();
			commandsBuffer.addFirst(UDSConstants.UDS_DEFAULT_SESSION);

			final Supplier<List<ObdCommand>> commandsSupplier = getCommandsSupplier(init, adjustments, query);

			connectionManager.update(commandsSupplier.get());

			commandProducer.updateSettings(adjustments, commandsSupplier, diagnostics, init);

			log.info("Resuming command producer");
			commandProducer.resume();
			return WorkflowExecutionStatus.UPDATED;
		} else {
			log.warn("No workflow is running");
		}

		ts = System.currentTimeMillis() - ts;
		log.info("Workflow update operation took: {}", ts);		
		return WorkflowExecutionStatus.NOT_RUNNING;
	}

	@Override
	public WorkflowExecutionStatus start(@NonNull AdapterConnection connection, @NonNull Init init,
			@NonNull Adjustments adjustments, SniffingPolicy sniffingPolicy) {

		final Task task = (final ExecutorService executorService) -> {
	
			try {
	
				
				log.info("[Start Sniffing] Starting the sniffing workflow task.");

				final EventsPublishlisher<Reply<?>> eventsPublisher = EventsPublishlisher.builder()
						.observer(new RoutinesResponseObserver<>(subscription))
						.observer(externalEventsObserver)
						.observer((ReplyObserver<Reply<?>>) alerts)
						.observer((ReplyObserver<Reply<?>>) diagnostics)
						.build();

		
				connectionManager = new ConnectionManager(connection, adjustments, 
						subscription, 
						eventsPublisher, 
						commandsBuffer);
	
				lifecycle.forEach(l -> {
					subscription.subscribe(l);
				});
				
				final CodecRegistry codecRegistry = CodecRegistry.builder()
						.registry(registry)
						.formulaEvaluatorConfig(formulaEvaluatorConfig)
						.subscription(subscription)
						.adjustments(adjustments).build();
				
				initCommandBuffer(init, adjustments);
	
				final PidDefinition sniffingPID = SniffingSupport.pid(sniffingPolicy);
				getPidRegistry().register(sniffingPID);
	
				this.commandProducer = buildCommandProducer(adjustments,
						getCommandsSupplier(init, adjustments, Query.builder().pid(sniffingPID.getId()).build()), init);

				final CommandLoop commandLoopThread = new CommandLoop(commandsBuffer, connectionManager, subscription,
						commandProducer, registry, connectoreResponseBuffer, eventsPublisher);
				
				final ConnectorResponseDecoder connectorResponseDecoderThread = new ConnectorResponseDecoder(
						connectoreResponseBuffer, adjustments, registry, codecRegistry, eventsPublisher);
	
				subscription.subscribe(connectorResponseDecoderThread);
				subscription.subscribe(commandProducer);
				subscription.subscribe(commandLoopThread);
				subscription.subscribe(connectionManager);
				subscription.onConnecting();
		
				log.info("[Start Sniffing] Context has been initialized");
			
	
				executorService.invokeAll(
						Arrays.asList(commandLoopThread, commandProducer, connectorResponseDecoderThread));
	
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
	
		return WorkflowOrchestrator.instance().submit(this, task);
	}

	@Override
	public WorkflowExecutionStatus start(@NonNull AdapterConnection connection, @NonNull Query query,
			@NonNull Init init, @NonNull Adjustments adjustments) {

		
		final Task task = (final ExecutorService executorService) -> {

			try {

				log.info("[Start] Starting the Workflow task.");
				log.info("[Start] Selected PID's: {}", query.getPids());
				log.info("[Start] Protocol: {}, headers: {}", init.getProtocol(), init.getHeaders());
				log.info("[Start] Debug: {}", adjustments.isDebugEnabled());
				log.info("[Start] Batch policy: {}", adjustments.getBatchPolicy());
				log.info("[Start] Stn extension: {}", adjustments.getStNxx());
	
				debugPIDs(query, init, adjustments);
	
				final EventsPublishlisher<Reply<?>> eventsPublisher = EventsPublishlisher.builder()
						.observer(new RoutinesResponseObserver<>(subscription))
						.observer(externalEventsObserver)
						.observer((ReplyObserver<Reply<?>>) alerts)
						.observer((ReplyObserver<Reply<?>>) diagnostics)
						.build();
		
				connectionManager = new ConnectionManager(connection, adjustments, 
						subscription, 
						eventsPublisher, 
						commandsBuffer);
				
				lifecycle.forEach(l -> {
					subscription.subscribe(l);
				});
			
				final CodecRegistry codecRegistry = CodecRegistry
						.builder()
						.registry(registry)
						.subscription(subscription)
						.formulaEvaluatorConfig(formulaEvaluatorConfig)
						.adjustments(adjustments).build();
				
				initCommandBuffer(init, adjustments);
				this.commandProducer = buildCommandProducer(adjustments,
						getCommandsSupplier(init, adjustments, query), init);

				final CommandLoop commandLoopThread = new CommandLoop(commandsBuffer, connectionManager, subscription,
						commandProducer, registry, connectoreResponseBuffer, eventsPublisher);

				final ConnectorResponseDecoder connectorResponseDecoderThread = new ConnectorResponseDecoder(
						connectoreResponseBuffer, adjustments, registry, codecRegistry, eventsPublisher);
	
				subscription.subscribe(connectorResponseDecoderThread);
				subscription.subscribe(commandProducer);
				subscription.subscribe(commandLoopThread);
				subscription.subscribe(connectionManager);
				subscription.onConnecting();
		
				log.info("[Start] Context has been initialized");
			
				alerts.reset();
				diagnostics.reset();
	
				executorService.invokeAll(
						Arrays.asList(commandLoopThread, commandProducer, connectorResponseDecoderThread));
	
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
		
		return WorkflowOrchestrator.instance().submit(this, task);
	}

	private void debugPIDs(Query query, Init init, Adjustments adjustments) {

		final Map<String, Header> canHeaders = init.getHeaders().stream()
				.collect(Collectors.toMap(Header::getMode, Function.identity()));

		final ObjectMapper objMapper = new ObjectMapper();

		query.getPids().forEach(id -> {
			final PidDefinition pid = registry.findBy(id);

			if (pid == null) {
				log.error("There is no PID available for id={} within provided resource files.", id);
			} else {

				String mode = pid.getMode();
				final boolean hasOverrides = pid.getOverrides().getCanMode() != null
						&& pid.getOverrides().getCanMode().length() > 0;
				if (hasOverrides) {
					mode = pid.getOverrides().getCanMode();
				}
				String header = "";
				if (canHeaders.containsKey(mode)) {
					header = canHeaders.get(mode).getHeader();
				}

				log.info("Mapping for a PID=[{}:{}] is: mode={}, header={}, hasOverrides={}", id, pid.getPid(), mode,
						header, hasOverrides);

				if (adjustments != null && adjustments.isDebugEnabled()) {

					try {
						final String serialized = objMapper.writeValueAsString(pid);
						log.info("PID=[{}:{}] body= \n{}", id, pid.getPid(), serialized);
					} catch (JsonProcessingException e) {
						log.warn("Failed to serialize PID to string");
					}
				}
			}
		});
	}

	private void notifyStopped() {
		log.info("Notyfing workflow is stopped");
		subscription.onStopped();	
	}

	private CommandProducer buildCommandProducer(Adjustments adjustements, Supplier<List<ObdCommand>> supplier,
			Init init) {
		return new CommandProducer(diagnostics, supplier, adjustements, init, commandsBuffer);
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
	
	
	void initCommandBuffer(Init init, Adjustments adjustements) {
	
		commandsBuffer.clear();
		init.getSequence().getCommands().stream().forEach(c -> {
			if (c instanceof DelayCommand) {
				((DelayCommand) c).setDelay(init.getDelayAfterReset());
			}
		});
		commandsBuffer.add(init.getSequence());

		if (null != adjustements.getSniffing()) {
			SniffingSupport.updateCommandBuffer(adjustements.getSniffing(), commandsBuffer);
		}

		commandsBuffer.addLast(new ATCommand("SP" + init.getProtocol().getType()));

		adjustements.getRequestedGroups().forEach(group -> {
			final List<Command> commands = registry.findBy(group).stream().filter(p -> p.getStable())
					.map(p -> new ObdCommand(p)).collect(Collectors.toList());
			final CANMessageHeaderManager headerManager = new CANMessageHeaderManager(init, commandsBuffer);
			headerManager.testSingleMode(commands);

			commands.forEach(command -> {
				headerManager.switchHeader(command);
				commandsBuffer.addLast(command);
			});
		});

		commandsBuffer.addLast(new DelayCommand(init.getDelayAfterInit()));
		commandsBuffer.addLast(new InitCompletedCommand());	
	}
}
