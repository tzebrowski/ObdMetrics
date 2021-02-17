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
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.ProducerPolicy;
import org.obd.metrics.StatusObserver;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.connection.Connection;
import org.obd.metrics.pid.PidRegistry;
import org.obd.metrics.statistics.StatisticsAccumulator;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class Workflow {
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

	public abstract void start();

	protected Connection connection;
	protected Set<Long> filter;
	protected boolean batchEnabled;

	public Workflow batch(boolean batchEnabled) {
		this.batchEnabled = batchEnabled;
		return this;
	}

	public Workflow filter(Set<Long> filter) {
		this.filter = filter;
		return this;
	}

	public Workflow connection(Connection connection) {
		this.connection = connection;
		return this;
	}

	@Builder(builderMethodName = "mode1")
	public static Workflow newMode1Workflow(@NonNull EcuSpecific ecuSpecific, @NonNull String equationEngine,
			@NonNull ReplyObserver observer, StatusObserver statusObserver, boolean enableStatistics, boolean generator)
			throws IOException {

		final Workflow workflow = new Mode1Workflow(ecuSpecific);
		workflow.replyObserver = observer;
		workflow.codec = CodecRegistry.builder().equationEngine(equationEngine).generator(generator).build();
		workflow.status = statusObserver == null ? StatusObserver.DEFAULT : statusObserver;
		return workflow;
	}

	@Builder(builderMethodName = "generic", builderClassName = "GenericBuilder")
	public static Workflow newGenericWorkflow(@NonNull EcuSpecific ecuSpecific, @NonNull String equationEngine,
			@NonNull ReplyObserver observer, StatusObserver statusObserver, boolean generator) throws IOException {

		final Workflow workflow = new GenericWorkflow(ecuSpecific);
		workflow.replyObserver = observer;
		workflow.codec = CodecRegistry.builder().equationEngine(equationEngine).generator(generator).build();
		workflow.status = statusObserver == null ? StatusObserver.DEFAULT : statusObserver;
		return workflow;
	}

	Workflow(EcuSpecific ecuSpecific) throws IOException {
		this.ecuSpecific = ecuSpecific;

		try (final InputStream stream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(ecuSpecific.getPidFile())) {
			this.pids = PidRegistry.builder().source(stream).build();
		}
	}

	public void stop() {
		log.info("Stopping the workflow: {}", getClass().getSimpleName());
		comandsBuffer.addFirst(new QuitCommand());
		status.onStopping();
	}
}
