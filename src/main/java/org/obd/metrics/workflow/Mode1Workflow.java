package org.obd.metrics.workflow;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.obd.metrics.CommandLoop;
import org.obd.metrics.command.group.Mode1CommandGroup;
import org.obd.metrics.command.process.InitCompletedCommand;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class Mode1Workflow extends Workflow {

	Mode1Workflow() throws IOException {
		super("mode01.json");
	}

	@Override
	public void start() {
		final Runnable task = () -> {
			
			final Set<String> newFilter = filter == null ? Collections.emptySet() : filter.stream().map(p-> p.toLowerCase() ).collect(Collectors.toSet());

			status.onConnecting();

			comandsBuffer.clear();
			comandsBuffer.add(Mode1CommandGroup.INIT);
			comandsBuffer.add(Mode1CommandGroup.SUPPORTED_PIDS);
			comandsBuffer.add(new InitCompletedCommand());
			
			log.info("Starting the workflow: {}. Selected PID's: {}", getClass().getSimpleName(), filter);

			var producer = new Mode1Producer(comandsBuffer,producerPolicy,pids,newFilter,batchEnabled);
			var executor = CommandLoop
					.builder()
					.connection(connection)
					.buffer(comandsBuffer)
					.observer(producer)
					.observer(replyObserver)
					.observer(statistics)
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
