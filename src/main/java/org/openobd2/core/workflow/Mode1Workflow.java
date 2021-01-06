package org.openobd2.core.workflow;

import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.openobd2.core.CommandExecutor;
import org.openobd2.core.CommandReplySubscriber;
import org.openobd2.core.CommandsBuffer;
import org.openobd2.core.ExecutorPolicy;
import org.openobd2.core.Mode1CommandsProducer;
import org.openobd2.core.ProducerPolicy;
import org.openobd2.core.codec.CodecRegistry;
import org.openobd2.core.command.process.QuitCommand;
import org.openobd2.core.connection.Connection;
import org.openobd2.core.pid.PidRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class Mode1Workflow implements Workflow {

	private final CommandsBuffer buffer = CommandsBuffer.instance(); // Define command buffer
	private final ProducerPolicy policy = ProducerPolicy.builder().frequency(50).build();
	private final ExecutorPolicy executorPolicy = ExecutorPolicy.builder().frequency(100).build();

	// just a single thread in a pool
	private static ExecutorService taskPool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(1), new ThreadPoolExecutor.DiscardPolicy());

	private final PidRegistry pidRegistry;
	private final CodecRegistry codecRegistry;
	private final CommandReplySubscriber subscriber;
	private final State state;

	Mode1Workflow(InputStream source, String evalEngine, CommandReplySubscriber subscriber, State state) {
		this.pidRegistry = PidRegistry.builder().source(source).build();
		this.codecRegistry = CodecRegistry.builder().evaluateEngine(evalEngine).pids(pidRegistry).build();
		this.subscriber = subscriber;
		this.state = state;
	}

	@Override
	public void start(Connection connection) {
		final Runnable task = () -> {

			state.starting();
			
			log.info("Starting the workflow: {}", getClass().getSimpleName());

			buffer.clear();
			final Mode1CommandsProducer producer = Mode1CommandsProducer
					.builder().buffer(buffer)
					.pidDefinitionRegistry(pidRegistry)
					.policy(policy)
					.build();

			final CommandExecutor executor = CommandExecutor
					.builder()
					.connection(connection)
					.buffer(buffer)
					.subscribe(producer)
					.subscribe(subscriber)
					.policy(executorPolicy)
					.codecRegistry(codecRegistry).build();

			final ExecutorService executorService = Executors.newFixedThreadPool(2);

			try {
				executorService.invokeAll(Arrays.asList(executor, producer));
			} catch (InterruptedException e) {
				log.error("Failed to schedule workers.", e);
			} finally {
				state.completed();
				executorService.shutdown();
			}
		};

		taskPool.submit(task);
	}

	@Override
	public void stop() {
		
		log.info("Stopping the workflow: {}", getClass().getSimpleName());
		buffer.addFirst(new QuitCommand());
		state.stopping();
	}
}
