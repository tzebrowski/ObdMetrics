package org.obd.metrics.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.obd.metrics.CommandLoopPolicy;
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

	protected EcuSpecific ecuSpecific;

	protected final CommandsBuffer comandsBuffer = CommandsBuffer.DEFAULT;
	protected final ProducerPolicy producerPolicy = ProducerPolicy.DEFAULT;
	protected CommandLoopPolicy executorPolicy = CommandLoopPolicy.DEFAULT;

	// just a single thread in a pool
	protected static ExecutorService singleTaskPool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
	        new LinkedBlockingQueue<Runnable>(1), new ThreadPoolExecutor.DiscardPolicy());

	@Getter
	protected final StatisticsAccumulator statistics = new StatisticsAccumulator();

	@Getter
	protected final PidRegistry pids;

	protected CodecRegistry codec;
	protected ReplyObserver replyObserver;
	protected Lifecycle lifecycle;

	@Override
	public void stop() {
		log.info("Stopping the workflow: {}", getClass().getSimpleName());
		lifecycle.onStopping();
		comandsBuffer.addFirst(new QuitCommand());
	}

	AbstractWorkflow(@NonNull EcuSpecific ecuSpecific, String equationEngine, @NonNull ReplyObserver observer,
	        Lifecycle statusObserver, Long commandFrequency, GeneratorSpec generatorSpec) throws IOException {
		this.ecuSpecific = ecuSpecific;

		this.replyObserver = observer;
		this.codec = CodecRegistry.builder().equationEngine(getEquationEngine(equationEngine))
		        .generatorSpec(generatorSpec).build();
		this.lifecycle = getLifecycle(statusObserver);

		var resources = Urls.toStreams(ecuSpecific.getFiles());
		try {
			this.pids = PidRegistry.builder().sources(resources).build();
		} finally {
			closeResources(resources);
		}

		if (commandFrequency != null) {
			executorPolicy = CommandLoopPolicy.builder().frequency(commandFrequency).build();
		}
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
