package org.obd.metrics.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.obd.metrics.CommandLoopPolicy;
import org.obd.metrics.CommandsBuffer;
import org.obd.metrics.ProducerPolicy;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.StatusObserver;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.connection.Connection;
import org.obd.metrics.pid.PidRegistry;
import org.obd.metrics.statistics.StatisticsAccumulator;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class AbstractWorkflow implements Workflow {
	protected static final double DEFAULT_GENERATOR_INCREMENT = 5.0;

	protected EcuSpecific ecuSpecific;

	protected final CommandsBuffer comandsBuffer = CommandsBuffer.DEFAULT;
	protected final ProducerPolicy producerPolicy = ProducerPolicy.DEFAULT;
	protected final CommandLoopPolicy executorPolicy = CommandLoopPolicy.DEFAULT;

	// just a single thread in a pool
	protected static ExecutorService singleTaskPool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(1), new ThreadPoolExecutor.DiscardPolicy());

	@Getter
	protected final StatisticsAccumulator statistics = new StatisticsAccumulator();

	@Getter
	protected final PidRegistry pids;

	protected CodecRegistry codec;
	protected ReplyObserver replyObserver;
	protected StatusObserver status;

	@Override
	public abstract void start();

	protected Connection connection;
	protected Set<Long> filter;
	protected boolean batchEnabled;

	@Override
	public void stop() {
		log.info("Stopping the workflow: {}", getClass().getSimpleName());
		comandsBuffer.addFirst(new QuitCommand());
		status.onStopping();
	}

	@Override
	public Workflow batch(boolean batchEnabled) {
		this.batchEnabled = batchEnabled;
		return this;
	}

	@Override
	public Workflow filter(Set<Long> filter) {
		this.filter = filter;
		return this;
	}

	@Override
	public Workflow connection(Connection connection) {
		this.connection = connection;
		return this;
	}

	AbstractWorkflow(@NonNull EcuSpecific ecuSpecific, String equationEngine, @NonNull ReplyObserver observer,
			StatusObserver statusObserver, boolean enableGenerator, Double generatorIncrement) throws IOException {
		this.ecuSpecific = ecuSpecific;

		this.replyObserver = observer;
		this.codec = CodecRegistry.builder().equationEngine(getEquationEngine(equationEngine))
				.enableGenerator(enableGenerator).generatorIncrement(getGeneratorIncrement(generatorIncrement)).build();

		this.status = getStatusObserver(statusObserver);

		try (final InputStream stream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(ecuSpecific.getPidFile())) {
			this.pids = PidRegistry.builder().source(stream).build();
		}
	}

	private Double getGeneratorIncrement(Double generatorIncrement) {
		return generatorIncrement == null ? DEFAULT_GENERATOR_INCREMENT : generatorIncrement;
	}

	private static StatusObserver getStatusObserver(StatusObserver statusObserver) {
		return statusObserver == null ? StatusObserver.DEFAULT : statusObserver;
	}

	private static @NonNull String getEquationEngine(String equationEngine) {
		return equationEngine == null || equationEngine.length() == 0 ? "JavaScript" : equationEngine;
	}

}
