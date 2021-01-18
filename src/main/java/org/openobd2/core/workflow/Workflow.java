package org.openobd2.core.workflow;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.openobd2.core.CommandReplySubscriber;
import org.openobd2.core.CommandsBuffer;
import org.openobd2.core.ExecutorPolicy;
import org.openobd2.core.ProducerPolicy;
import org.openobd2.core.StatusObserver;
import org.openobd2.core.codec.CodecRegistry;
import org.openobd2.core.command.process.QuitCommand;
import org.openobd2.core.connection.Connection;
import org.openobd2.core.pid.PidRegistry;

import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class Workflow {

	protected final CommandsBuffer buffer = CommandsBuffer.instance();
	protected final ProducerPolicy policy = ProducerPolicy.builder().delayBeforeInsertingCommands(50)
			.emptyBufferSleepTime(200).build();

	protected final ExecutorPolicy executorPolicy = ExecutorPolicy.builder().frequency(100).delayBeforeExecution(20)
			.build();

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

	abstract void start(Connection connection, Set<String> pids);

	@Builder(builderMethodName = "mode1")
	public static Workflow newMode1Workflow(@NonNull String equationEngine, @NonNull CommandReplySubscriber subscriber,
			StatusObserver statusObserver, boolean batchEnabled) throws IOException {
		return new Mode1Workflow(equationEngine, subscriber, statusObserver, batchEnabled);
	}

	public static Workflow generic(@NonNull EcuSpecific ecuSpecific, @NonNull String equationEngine,
			@NonNull CommandReplySubscriber subscriber, StatusObserver statusObserver) throws IOException {
		return new GenericWorkflow(ecuSpecific, equationEngine, subscriber, statusObserver);
	}

	Workflow(String equationEngine, CommandReplySubscriber subscriber, StatusObserver statusListener,
			String resourceFile) throws IOException {

		this.subscriber = subscriber;
		this.statusObserver = statusListener == null ? StatusObserver.DUMMY : statusListener;

		try (final InputStream stream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(resourceFile)) {
			this.pidRegistry = PidRegistry.builder().source(stream).build();
		}
		this.codecRegistry = CodecRegistry.builder().equationEngine(equationEngine).pids(this.pidRegistry).build();
	}

	public void start(Connection connection) {
		start(connection, Collections.emptySet());
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
