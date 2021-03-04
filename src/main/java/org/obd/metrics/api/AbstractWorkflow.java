package org.obd.metrics.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.obd.metrics.CommandsBuffer;
import org.obd.metrics.Lifecycle;
import org.obd.metrics.ProducerPolicy;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.pid.PidRegistry;
import org.obd.metrics.pid.Urls;
import org.obd.metrics.statistics.StatisticsAccumulator;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class AbstractWorkflow implements Workflow {

	protected PidSpec pidSpec;

	protected final CommandsBuffer comandsBuffer = new CommandsBuffer();
	protected ProducerPolicy producerPolicy = ProducerPolicy.DEFAULT;

	// just a single thread in a pool
	protected static ExecutorService singleTaskPool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
	        new LinkedBlockingQueue<Runnable>(1), new ThreadPoolExecutor.DiscardPolicy());

	@Getter
	protected final StatisticsAccumulator statistics = new StatisticsAccumulator();

	@Getter
	protected final PidRegistry pids;

	protected ReplyObserver replyObserver;
	protected Lifecycle lifecycle;
	protected final String equationEngine;

	@Override
	public void stop() {
		log.info("Stopping the workflow: {}", getClass().getSimpleName());
		comandsBuffer.addFirst(new QuitCommand());
		lifecycle.onStopping();
	}

	AbstractWorkflow(PidSpec pidSpec, String equationEngine, ReplyObserver observer,
	        Lifecycle statusObserver, Long commandFrequency) throws IOException {
		this.pidSpec = pidSpec;
		this.equationEngine = equationEngine;
		this.replyObserver = observer;

		this.lifecycle = getLifecycle(statusObserver);

		var resources = Urls.toStreams(pidSpec.getSources());
		try {
			this.pids = PidRegistry.builder().sources(resources).build();
		} finally {
			closeResources(resources);
		}

		if (commandFrequency != null) {
			producerPolicy = ProducerPolicy.builder().delayBeforeInsertingCommands(commandFrequency).build();
		}
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
