package org.obd.metrics.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Executors;

import org.obd.metrics.CommandLoop;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.StatusObserver;
import org.obd.metrics.command.group.Mode1CommandGroup;
import org.obd.metrics.command.process.InitCompletedCommand;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class Mode1Workflow extends AbstractWorkflow {

	Mode1Workflow(@NonNull EcuSpecific ecuSpecific, String equationEngine,
			@NonNull ReplyObserver observer, StatusObserver statusObserver, boolean enableGenerator,
			Double generatorIncrement) throws IOException {
		super(ecuSpecific,equationEngine,observer,statusObserver,enableGenerator,generatorIncrement);
	}

	@Override
	public void start() {
	
		final Runnable task = () -> {

			status.onConnecting();
			comandsBuffer.clear();
			comandsBuffer.add(ecuSpecific.getInitSequence());
			comandsBuffer.add(Mode1CommandGroup.SUPPORTED_PIDS);
			comandsBuffer.add(new InitCompletedCommand());

			log.info("Starting the workflow: {}. Selected PID's: {}", getClass().getSimpleName(), filter);

			var producer = new Mode1Producer(comandsBuffer, producerPolicy, pids, filter, batchEnabled);
			
			var executor = CommandLoop
					.builder()
					.connection(connection)
					.buffer(comandsBuffer)
					.observer(producer)
					.observer(replyObserver)
					.observer(statistics)
					.pids(pids)
					.policy(executorPolicy)
					.codecRegistry(codec)
					.statusObserver(status)
					.build();

			var executorService = Executors.newFixedThreadPool(2);

			try {
				executorService.invokeAll(Arrays.asList(executor, producer));
			} catch (InterruptedException e) {
				log.error("Failed to schedule workers.", e);
			} finally {
				status.onStopped();
				executorService.shutdown();
			}
		};

		singleTaskPool.submit(task);
	}
}
