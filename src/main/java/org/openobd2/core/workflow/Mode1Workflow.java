package org.openobd2.core.workflow;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.openobd2.core.CommandExecutor;
import org.openobd2.core.CommandsBuffer;
import org.openobd2.core.DataCollector;
import org.openobd2.core.ExecutorPolicy;
import org.openobd2.core.Mode1CommandsProducer;
import org.openobd2.core.ProducerPolicy;
import org.openobd2.core.codec.CodecRegistry;
import org.openobd2.core.command.group.Mode1CommandGroup;
import org.openobd2.core.command.process.QuitCommand;
import org.openobd2.core.pid.PidRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class Mode1Workflow implements Workflow {

	private final CommandsBuffer buffer = CommandsBuffer.instance(); // Define command buffer
	private final ProducerPolicy policy = ProducerPolicy.builder().frequency(50).build();
	private final ExecutorPolicy executorPolicy = ExecutorPolicy.builder().frequency(100).build();
	private final DataCollector collector = new DataCollector(); // It collects the

	// just a single thread in a pool
	private static ExecutorService svc = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(1), new ThreadPoolExecutor.DiscardPolicy());

	@Override
	public void start(WorkflowSpec spec) throws Exception {
		final Runnable task = () -> {
			
			final PidRegistry pidRegistry = PidRegistry.builder().source(spec.getSource()).build();

			final CodecRegistry codecRegistry = CodecRegistry.builder().evaluateEngine(spec.getEvaluationEngine())
					.pids(pidRegistry).build();

			final Mode1CommandsProducer producer = Mode1CommandsProducer.builder().buffer(buffer)
					.pidDefinitionRegistry(pidRegistry).policy(policy).build();

			final CommandExecutor executor = CommandExecutor
					.builder()
					.connection(spec.getConnection())
					.buffer(buffer)
					.subscribe(collector)
					.subscribe(spec.getSubscriber())
					.policy(executorPolicy).codecRegistry(codecRegistry).build();

			final ExecutorService executorService = Executors.newFixedThreadPool(2);

			try {
				executorService.invokeAll(Arrays.asList(executor, producer));
			} catch (InterruptedException e) {
				log.error("Failed to schedule workers.", e);
			} finally {
				executorService.shutdown();
			}
		};
		
		svc.submit(task);
	}

	@Override
	public void stop() {
		  buffer.addFirst(new QuitCommand()); // quit the CommandExecutor
	}

}
