package org.obd.metrics.api;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.obd.metrics.CommandLoop;
import org.obd.metrics.Lifecycle;
import org.obd.metrics.Reply;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.codec.GeneratorSpec;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.connection.AdapterConnection;
import org.obd.metrics.diagnostic.Diagnostics;
import org.obd.metrics.pid.PidDefinitionRegistry;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class AbstractWorkflow implements Workflow {

	protected PidSpec pidSpec;
	protected CommandProducer commandProducer;

	protected final CommandsBuffer commandsBuffer = CommandsBuffer.instance();

	@Getter
	protected Diagnostics diagnostics = Diagnostics.instance();

	@Getter
	protected final PidDefinitionRegistry pidRegistry;

	protected ReplyObserver<Reply<?>> replyObserver;
	protected final String equationEngine;
	protected final Lifecycle.LifecycleSubscriber lifecycle = new Lifecycle.LifecycleSubscriber();

	// just a single thread in a pool
	private static final ExecutorService singleTaskPool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
	        new LinkedBlockingQueue<Runnable>(1), new ThreadPoolExecutor.DiscardPolicy());

	abstract void init(Adjustments adjustments);

	abstract CommandsSuplier getCommandsSupplier(Adjustments adjustements, Query query);

	protected AbstractWorkflow(PidSpec pidSpec, String equationEngine, ReplyObserver<Reply<?>> observer,
	        Lifecycle lifecycle) {
		
		log.info("Creating an instance of the '{}' workflow", getClass().getSimpleName());

		this.pidSpec = pidSpec;
		this.equationEngine = equationEngine;
		this.replyObserver = observer;

		this.lifecycle.subscribe(lifecycle);

		try (final Sources sources = Sources.open(pidSpec)) {
			this.pidRegistry = PidDefinitionRegistry.builder().sources(sources.getResources()).build();
		}
	}

	@Override
	public void stop() {
		log.info("Stopping the workflow: {}", getClass().getSimpleName());
		commandsBuffer.addFirst(new QuitCommand());
		log.info("Publishing lifecycle changes");
		lifecycle.onStopping();
	}

	@Override
	public void start(@NonNull AdapterConnection connection, @NonNull Query query, @NonNull Adjustments adjustements) {

		final Runnable task = () -> {
			final ExecutorService executorService = Executors.newFixedThreadPool(2);

			try {

				init(adjustements);

				log.info("Starting the workflow: {}.Adjustements: {}, selected PID's: {}",
				        getClass().getSimpleName(), adjustements, query.getPids());

				diagnostics.reset();

				final CommandsSuplier commandsSupplier = getCommandsSupplier(adjustements,
				        query);
				lifecycle.subscribe(commandsSupplier);

				commandProducer = getProducer(adjustements, commandsSupplier);

				@SuppressWarnings("unchecked")
				final CommandLoop commandLoop = CommandLoop
				        .builder()
				        .connection(connection)
				        .buffer(commandsBuffer)
				        .observers(getObservers())
				        .observer(replyObserver)
				        .observer((ReplyObserver<Reply<?>>) diagnostics)
				        .pids(pidRegistry)
				        .codecs(getCodecRegistry(adjustements.getGenerator()))
				        .lifecycle(lifecycle).build();

				executorService.invokeAll(Arrays.asList(commandLoop, commandProducer));

			} catch (Throwable e) {
				log.error("Failed to initialize the framework.", e);
			} finally {
				log.info("Stopping the Workflow.");
				lifecycle.onStopped();
				executorService.shutdown();
			}
		};

		singleTaskPool.submit(task);
	}

	protected CommandProducer getProducer(Adjustments adjustements, CommandsSuplier supplier) {
		return new CommandProducer(diagnostics, commandsBuffer, supplier, adjustements);
	}

	protected CodecRegistry getCodecRegistry(GeneratorSpec generatorSpec) {
		return CodecRegistry.builder().equationEngine(getEquationEngine(equationEngine)).generatorSpec(generatorSpec)
		        .build();
	}

	protected @NonNull String getEquationEngine(String equationEngine) {
		return equationEngine == null || equationEngine.length() == 0 ? "JavaScript" : equationEngine;
	}

	List<ReplyObserver<Reply<?>>> getObservers() {
		return Arrays.asList(commandProducer);
	}
}
