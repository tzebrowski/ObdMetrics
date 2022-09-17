package org.obd.metrics.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.command.ATCommand;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.dtc.DtcCommand;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.command.meta.HexCommand;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.DelayCommand;
import org.obd.metrics.command.process.InitCompletedCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.context.Context;
import org.obd.metrics.diagnostic.Diagnostics;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.pid.PidType;
import org.obd.metrics.transport.AdapterConnection;
import org.obd.metrics.transport.Connector;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DefaultWorkflow implements Workflow {

	@Getter
	private Diagnostics diagnostics = Diagnostics.instance();

	@Getter
	private final PidDefinitionRegistry pidRegistry;

	private ReplyObserver<Reply<?>> externalEventsObserver;

	private final Lifecycle lifecycle;
	private final FormulaEvaluatorConfig formulaEvaluatorConfig;

	// just a single thread in a pool
	private static final ExecutorService singleTaskPool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(1), new ThreadPoolExecutor.DiscardPolicy());

	protected DefaultWorkflow(Pids pids, FormulaEvaluatorConfig formulaEvaluatorConfig,
			ReplyObserver<Reply<?>> eventsObserver, Lifecycle lifecycle) {

		log.info("Creating an instance of the '{}' Workflow.", getClass().getSimpleName());
		this.formulaEvaluatorConfig = formulaEvaluatorConfig;
		this.externalEventsObserver = eventsObserver;
		this.lifecycle = lifecycle;
		this.pidRegistry = initPidDefinitionRegistry(pids);
	}
	
	
	
	@Override
	public void stop(boolean gracefulStop) {
		Context.apply(context -> {
			if (!gracefulStop) {
				context.resolve(Connector.class).apply(connector -> {
					try {
						log.info("Graceful stop is not enabled. Closing streams by force.");
						connector.close();
					} catch (IOException e) {
						// ignore
					}
				});
			}

			context.resolve(CommandsBuffer.class).apply(commandsBuffer -> {
				log.info("Stopping the Workflow task. Publishing QUIT command...");
				commandsBuffer.clear();
				commandsBuffer.addFirst(new QuitCommand());
			});

			context.resolve(Subscription.class).apply(p -> {
				log.info("Publishing lifecycle changes");
				p.onStopping();
			});
		});
	}

	@Override
	public void start(@NonNull AdapterConnection connection, @NonNull Query query, @NonNull Init init,
			@NonNull Adjustments adjustements) {

		final Runnable task = () -> {
			final ExecutorService executorService = Executors.newFixedThreadPool(2);

			try {

				log.info(
						"Starting the Workflow task.\n Protocol: {}, headers: {}, adjustements: {}, selected PID's: {}",
						init.getProtocol(), init.getHeaders(), adjustements, query.getPids());

				Context.apply(it -> {
					it.reset();
					it.register(PidDefinitionRegistry.class, pidRegistry);
					it.register(Subscription.class, new Subscription()).apply(p -> {
						p.subscribe(lifecycle);
					});
					
					it.register(CodecRegistry.class,
							CodecRegistry
							.builder()
							.formulaEvaluatorConfig(formulaEvaluatorConfig)
							.adjustments(adjustements).build())
						
						.apply(codecRegistry -> {
							DefaultCommandGroup.SUPPORTED_PIDS.getCommands().forEach(p -> {
								codecRegistry.register(p.getPid(), p);
						});
					});
					
					it.register(CommandsBuffer.class, CommandsBuffer.instance()).apply(commandsBuffer -> {
						commandsBuffer.clear();
						commandsBuffer.add(init.getSequence());

						if (adjustements.isVehicleCapabilitiesReadingEnabled()) {
							log.info("Fetch Metadata is enabled. Adding Metadata commands to the queue.");
							new CommandHandler().updateBuffer(PidType.METADATA, HexCommand.class, init);
						}

						if (adjustements.isVehicleDtcReadingEnabled()) {
							log.info("Fetch DTC is enabled. Adding DTC commands to the queue.");
							new CommandHandler().updateBuffer(PidType.DTC, DtcCommand.class, init);
						}
						
						// Protocol
						commandsBuffer.addLast(new ATCommand("SP" + init.getProtocol().getType()));
						if (adjustements.isVehicleCapabilitiesReadingEnabled()) {
							
							log.info("Adding commands to the queue to fetch supported PIDs.");
							final CANMessageHeaderManager headerManager = new CANMessageHeaderManager(init);
							final List<Command> commands = new ArrayList<Command>(DefaultCommandGroup.SUPPORTED_PIDS.getCommands());
							headerManager.testSingleMode(commands);
							commands.forEach(c -> {
								headerManager.switchHeader(c);
								commandsBuffer.addLast(c);
							});
						}

						commandsBuffer.addLast(new DelayCommand(init.getDelay()));
						commandsBuffer.addLast(new InitCompletedCommand());
					});
				});
				
				final CommandProducer commandProducerThread = buildCommandProducer(adjustements,
						getCommandsSupplier(adjustements, query), init);

				Context.apply(it -> {
					it.resolve(Subscription.class).apply(p -> {
						p.subscribe(commandProducerThread);
						p.onConnecting();
					});

					it.register(EventsPublishlisher.class, EventsPublishlisher.builder()
							.observer(externalEventsObserver).observer((ReplyObserver<Reply<?>>) diagnostics).build());
				});

				diagnostics.reset();
				executorService.invokeAll(Arrays.asList(new CommandLoop(connection), commandProducerThread));

			} catch (Throwable e) {
				log.error("Failed to initialize the Workflow task.", e);
			} finally {
				log.info("Stopping the Workflow task.");

				Context.instance().resolve(Subscription.class).apply(p -> {
					p.onStopped();
				});

				executorService.shutdown();
			}
		};

		log.info("Submitting the Workflow task.");
		singleTaskPool.submit(task);
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

		for (final ObdCommand p : DefaultCommandGroup.SUPPORTED_PIDS.getCommands()) {
			pidRegistry.register(p.getPid());
		}

		tt = System.currentTimeMillis() - tt;
		log.info("Loading resources files took: {}ms.", tt);
		return pidRegistry;
	}

	private Supplier<List<ObdCommand>> getCommandsSupplier(Adjustments adjustements, Query query) {
		return new CommandsSuplier(getPidRegistry(), adjustements, query);
	}
}
