package org.obd.metrics.api;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.command.ATCommand;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.DelayCommand;
import org.obd.metrics.command.process.InitCompletedCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.diagnostic.Diagnostics;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.AdapterConnection;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DefaultWorkflow implements Workflow {

	
	private final CommandsBuffer commandsBuffer = CommandsBuffer.instance();

	@Getter
	private Diagnostics diagnostics = Diagnostics.instance();

	@Getter
	private final PidDefinitionRegistry pidRegistry;

	private ReplyObserver<Reply<?>> externalEventsObserver;
	private final String equationEngine;
	private final Lifecycle.Subscription subscription = Lifecycle.subscription;
	private final Lifecycle externalSubsciber;

	// just a single thread in a pool
	private static final ExecutorService singleTaskPool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
	        new LinkedBlockingQueue<Runnable>(1), new ThreadPoolExecutor.DiscardPolicy());

	protected DefaultWorkflow(
	        Pids pids,
	        String equationEngine,
	        ReplyObserver<Reply<?>> eventsObserver,
	        Lifecycle lifecycle) {

		log.info("Creating an instance of the '{}' workflow", getClass().getSimpleName());
		this.equationEngine = equationEngine;
		this.externalEventsObserver = eventsObserver;
		this.externalSubsciber = lifecycle;

		try (final Resources sources = Resources.convert(pids)) {
			this.pidRegistry = PidDefinitionRegistry.builder().sources(sources.getResources()).build();
		}
	}

	@Override
	public void stop() {
		log.info("Stopping the workflow...");
		commandsBuffer.clear();
		commandsBuffer.addFirst(new QuitCommand());
		log.info("Publishing lifecycle changes");
		subscription.onStopping();
	}

	@Override
	public void start(@NonNull AdapterConnection connection, @NonNull Query query,
	        @NonNull Init init, @NonNull Adjustments adjustements) {

		final Runnable task = () -> {
			final ExecutorService executorService = Executors.newFixedThreadPool(2);

			try {
				
				subscription.unregisterAll();
				
				final CodecRegistry codec = buildCodecRegistry(adjustements);
				final CommandProducer commandProducer = buildCommandProducer(adjustements, getCommandsSupplier(adjustements,
				        query), init);

				initCommandBuffer(codec, init);
				initLifecycleSubscribtion(commandProducer);

				log.info("Starting the workflow. Protocol: {}, headers: {}, adjustements: {}, selected PID's: {}",
				        init.getProtocol(), init.getHeaders(), adjustements, query.getPids());

				diagnostics.reset();

				
				@SuppressWarnings("unchecked")
				final CommandLoop commandLoop = new CommandLoop(
						connection,
						commandsBuffer,
						subscription,
						codec,
						pidRegistry,
						Arrays.asList(externalEventsObserver,(ReplyObserver<Reply<?>>) diagnostics));

				executorService.invokeAll(Arrays.asList(commandLoop, commandProducer));

			} catch (Throwable e) {
				log.error("Failed to initialize the framework.", e);
			} finally {
				log.info("Stopping the Workflow.");
				subscription.onStopped();
				executorService.shutdown();
			}
		};

		singleTaskPool.submit(task);
	}

	private CommandProducer buildCommandProducer(Adjustments adjustements, Supplier<List<ObdCommand>> supplier, Init init) {
		return new CommandProducer(diagnostics, commandsBuffer, supplier, adjustements, init);
	}

	private CodecRegistry buildCodecRegistry(Adjustments adjustments) {
		return CodecRegistry.builder().equationEngine(getEquationEngine(equationEngine)).adjustments(adjustments)
		        .build();
	}

	private @NonNull String getEquationEngine(String equationEngine) {
		return equationEngine == null || equationEngine.length() == 0 ? "JavaScript" : equationEngine;
	}

	private void initLifecycleSubscribtion(CommandProducer commandProducer) {
		
		subscription.subscribe(externalSubsciber);
		subscription.subscribe(commandProducer);
		subscription.onConnecting();
	}

	private void initCommandBuffer(CodecRegistry codecRegistry, Init initConfiguration) {
		DefaultCommandGroup.SUPPORTED_PIDS.getCommands().forEach(p -> {
			codecRegistry.register(p.getPid(), p);
		});
		
		commandsBuffer.clear();
		commandsBuffer.add(initConfiguration.getSequence());

		if (initConfiguration.isFetchDeviceProperties()) {
			log.info("Add commands to the queue fetch devices properties.");
			commandsBuffer.add(DefaultCommandGroup.DEVICE_PROPERTIES);
		}
		
		// Protocol
		commandsBuffer.addLast(new ATCommand("SP" + initConfiguration.getProtocol().getType()));
		if (initConfiguration.isFetchSupportedPids()) {
			log.info("Add commands to the queue to fetch supported PIDs.");
			commandsBuffer.add(DefaultCommandGroup.SUPPORTED_PIDS);
		}
		
		commandsBuffer.addLast(new DelayCommand(initConfiguration.getDelay()));
		commandsBuffer.addLast(new InitCompletedCommand());
	}

	private Supplier<List<ObdCommand>> getCommandsSupplier(Adjustments adjustements, Query query) {
		return new CommandsSuplier(pidRegistry, adjustements.isBatchEnabled(),
		        query);
	}
}
