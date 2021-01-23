package org.obd.metrics.workflow;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.obd.metrics.CommandReplySubscriber;
import org.obd.metrics.CommandsBuffer;
import org.obd.metrics.ExecutorPolicy;
import org.obd.metrics.ProducerPolicy;
import org.obd.metrics.StatusObserver;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.connection.Connection;
import org.obd.metrics.pid.PidRegistry;

import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class Workflow {

	protected final CommandsBuffer buffer = CommandsBuffer.instance();
	protected final ProducerPolicy policy = ProducerPolicy.DEFAULT;
	protected final ExecutorPolicy executorPolicy = ExecutorPolicy.DEFAULT;

	// just a single thread in a pool
	protected static ExecutorService taskPool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(1), new ThreadPoolExecutor.DiscardPolicy());

	@NonNull
	protected final PidRegistry pidRegistry;

	@NonNull
	protected final CodecRegistry codecRegistry;

	@NonNull
	protected final CommandReplySubscriber subscriber;

	@NonNull
	protected final StatusObserver statusObserver;

	public abstract void start(Connection connection, Set<String> filter, boolean batchEnabled);

	@Builder(builderMethodName = "mode1")
	public static Workflow newMode1Workflow(@NonNull String equationEngine, @NonNull CommandReplySubscriber subscriber,
			StatusObserver statusObserver) throws IOException {
		return new Mode1Workflow(equationEngine, subscriber, statusObserver);
	}

	@Builder(builderMethodName = "generic", builderClassName = "GenericBuilder")
	public static Workflow newGenericWorkflow(@NonNull EcuSpecific ecuSpecific, @NonNull String equationEngine,
			@NonNull CommandReplySubscriber subscriber, StatusObserver statusObserver) throws IOException {
		return new GenericWorkflow(ecuSpecific, equationEngine, subscriber, statusObserver);
	}

	Workflow(String equationEngine, CommandReplySubscriber subscriber, StatusObserver statusListener,
			String resourceFile) throws IOException {

		this.subscriber = subscriber;
		this.statusObserver = statusListener == null ? StatusObserver.DEFAULT : statusListener;

		try (final InputStream stream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(resourceFile)) {
			this.pidRegistry = PidRegistry.builder().source(stream).build();
		}
		this.codecRegistry = CodecRegistry.builder().equationEngine(equationEngine).pids(this.pidRegistry).build();
	}

	public PidRegistry getRegistry() {
		return pidRegistry;
	}

	public void stop() {
		log.info("Stopping the workflow: {}", getClass().getSimpleName());
		buffer.addFirst(new QuitCommand());
		statusObserver.onStopping();
	}
}
