package org.obd.metrics.api;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Init;
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
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.AdapterConnection;
import org.obd.metrics.transport.Connector;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DefaultWorkflow implements Workflow {

	private transient Future<?> tasks;

	@Getter
	private Diagnostics diagnostics = Diagnostics.instance();

	@Getter
	private final PidDefinitionRegistry pidRegistry;

	private ReplyObserver<Reply<?>> externalEventsObserver;

	private final List<Lifecycle> lifecycle;
	private final FormulaEvaluatorConfig formulaEvaluatorConfig;
	
	
	// just a single thread in a pool
	private static final ExecutorService singleTaskPool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(1), new ThreadPoolExecutor.DiscardPolicy());

	protected DefaultWorkflow(Pids pids, FormulaEvaluatorConfig formulaEvaluatorConfig,
			ReplyObserver<Reply<?>> eventsObserver, List<Lifecycle> lifecycle) {

		log.info("Creating an instance of the Workflow task.");
		this.formulaEvaluatorConfig = formulaEvaluatorConfig;
		this.externalEventsObserver = eventsObserver;
		this.lifecycle = lifecycle;
		this.pidRegistry = initPidDefinitionRegistry(pids);
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

				context.resolve(CommandsBuffer.class).apply(commandsBuffer -> {

					log.info("Stopping the Workflow task.");
					try {
						log.debug("Publishing QUIT command...");
						commandsBuffer.addFirst(new QuitCommand());
					} catch (Exception e) {
						subscription.onError("Failed to add quite command", e);
					}

					try {
						log.debug("Deleting existing commands from the queue.");
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
	public void start(@NonNull AdapterConnection connection, @NonNull Query query, @NonNull Init init,
			@NonNull Adjustments adjustements) {
		
		final Runnable task = () -> {
			final ExecutorService executorService = Executors.newFixedThreadPool(3);

			try {

				log.info(
						"Starting the Workflow task.\n Protocol: {}, headers: {},DBEUG: {},selected PID's: {}, adjustements: {}",
						init.getProtocol(), init.getHeaders(), adjustements.isDebugEnabled(), query.getPids(),
						adjustements);

				final ConnectionManager connectionManager = new ConnectionManager(connection, adjustements);
				
				Context.apply(it -> {
					it.reset();
					it.register(PidDefinitionRegistry.class, pidRegistry);
					it.register(Subscription.class, new Subscription()).apply(p -> {
						lifecycle.forEach(l-> {
							p.subscribe(l);
						});
					});
					it.register(ConnectorResponseBuffer.class, ConnectorResponseBuffer.instance());
					it.register(CodecRegistry.class, CodecRegistry.builder()
							.formulaEvaluatorConfig(formulaEvaluatorConfig).adjustments(adjustements).build());
					it.register(ConnectionManager.class, connectionManager);
					prepareInitBuffer(init, adjustements, it);
				});

				final CommandProducer commandProducer = buildCommandProducer(adjustements,
						getCommandsSupplier(init, adjustements, query), init);
				final CommandLoop commandLoop = new CommandLoop();
				final ConnectorResponseDecoder connectorResponseDecoder = new ConnectorResponseDecoder(adjustements);

				Context.apply(it -> {
					it.resolve(Subscription.class).apply(p -> {
						p.subscribe(connectorResponseDecoder);
						p.subscribe(commandProducer);
						p.subscribe(commandLoop);
						p.subscribe(connectionManager);
						p.onConnecting();
					});
					it.register(EventsPublishlisher.class, EventsPublishlisher.builder()
							.observer(externalEventsObserver).observer((ReplyObserver<Reply<?>>) diagnostics).build());
				});

				connectionManager.init();
				
				diagnostics.reset();
				executorService.invokeAll(Arrays.asList(commandLoop, commandProducer, connectorResponseDecoder));

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
					log.error("Error occured while stopping the workflow.",e);
				}
			}
		};

		log.info("Submitting the Workflow task.");
		tasks = singleTaskPool.submit(task);
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

	private PidDefinitionRegistry initPidDefinitionRegistry(Pids pids) {
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
