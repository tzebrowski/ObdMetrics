package org.obd.metrics.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.obd.metrics.CommandLoop;
import org.obd.metrics.CommandsBuffer;
import org.obd.metrics.Lifecycle;
import org.obd.metrics.ProducerPolicy;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.pid.PidRegistry;
import org.obd.metrics.pid.Urls;
import org.obd.metrics.statistics.StatisticsRegistry;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class AbstractWorkflow implements Workflow {

	protected PidSpec pidSpec;

	protected final CommandsBuffer comandsBuffer = new CommandsBuffer();
	protected ProducerPolicy producerPolicy = ProducerPolicy.DEFAULT;

	@Getter
	protected final StatisticsRegistry statisticsRegistry = StatisticsRegistry.builder().build();

	@Getter
	protected final PidRegistry pidRegistry;

	protected ReplyObserver replyObserver;
	protected final String equationEngine;
	protected Lifecycle lifecycle;

	// just a single thread in a pool
	private static final ExecutorService singleTaskPool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
	        new LinkedBlockingQueue<Runnable>(1), new ThreadPoolExecutor.DiscardPolicy());

	abstract void init();

	abstract Producer getProducer(WorkflowContext ctx);

	protected AbstractWorkflow(PidSpec pidSpec, String equationEngine, ReplyObserver observer,
	        Lifecycle statusObserver, Long commandFrequency) throws IOException {
		this.pidSpec = pidSpec;
		this.equationEngine = equationEngine;
		this.replyObserver = observer;

		this.lifecycle = getLifecycle(statusObserver);

		var resources = Urls.toStreams(pidSpec.getSources());
		try {
			this.pidRegistry = PidRegistry.builder().sources(resources).build();
		} finally {
			closeResources(resources);
		}

		if (commandFrequency != null) {
			producerPolicy = ProducerPolicy.builder().beforeFeelingQueue(commandFrequency).build();
		}
	}

	@Override
	public void stop() {
		log.info("Stopping the workflow: {}", getClass().getSimpleName());
		comandsBuffer.addFirst(new QuitCommand());
		log.info("Publishing lifecycle changes");
		lifecycle.onStopping();
	}

	@Override
	public void start(WorkflowContext ctx) {

		final Runnable task = () -> {
			var executorService = Executors.newFixedThreadPool(2);

			try {

				init();

				log.info("Starting the workflow: {}. Batch enabled: {},generator: {}, selected PID's: {}",
				        getClass().getSimpleName(), ctx.isBatchEnabled(), ctx.generator, ctx.filter);

				final Producer producer = getProducer(ctx);

				var executor = CommandLoop
				        .builder()
				        .connection(ctx.connection)
				        .buffer(comandsBuffer)
				        .observer(producer)
				        .observer(replyObserver)
				        .observer((ReplyObserver) statisticsRegistry)
				        .pids(pidRegistry)
				        .codecRegistry(getCodecRegistry(ctx.generator))
				        .lifecycle(lifecycle)
				        .build();

				executorService.invokeAll(Arrays.asList(executor, producer));

			} catch (InterruptedException e) {
				log.error("Failed to schedule workers.", e);
			} finally {
				log.info("Stopping the Workflow.");
				lifecycle.onStopped();
				executorService.shutdown();
			}
		};

		singleTaskPool.submit(task);
	}

	protected CodecRegistry getCodecRegistry(GeneratorSpec generatorSpec) {
		return CodecRegistry.builder().equationEngine(getEquationEngine(equationEngine)).generatorSpec(generatorSpec)
		        .build();
	}

	private void closeResources(List<InputStream> resources) {
		resources.forEach(f -> {
			try {
				f.close();
			} catch (IOException e) {
			}
		});
	}

	private static Lifecycle getLifecycle(Lifecycle lifecycle) {
		return lifecycle == null ? Lifecycle.DEFAULT : lifecycle;
	}

	private static @NonNull String getEquationEngine(String equationEngine) {
		return equationEngine == null || equationEngine.length() == 0 ? "JavaScript" : equationEngine;
	}
}
