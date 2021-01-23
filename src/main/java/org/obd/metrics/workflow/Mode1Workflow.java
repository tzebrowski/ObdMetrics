package org.obd.metrics.workflow;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.obd.metrics.CommandExecutor;
import org.obd.metrics.CommandReplySubscriber;
import org.obd.metrics.StatusObserver;
import org.obd.metrics.command.group.Mode1CommandGroup;
import org.obd.metrics.command.process.InitCompletedCommand;
import org.obd.metrics.connection.Connection;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class Mode1Workflow extends Workflow {

	Mode1Workflow(String equationEngine, CommandReplySubscriber subscriber, StatusObserver state) throws IOException {
		super(equationEngine, subscriber, state, "mode01.json");
	}

	@Override
	public void start(Connection connection, Set<String> filter, boolean batchEnabled) {
		final Runnable task = () -> {
			
			final Set<String> newFilter = filter == null ? Collections.emptySet() : filter.stream().map(p-> p.toLowerCase() ).collect(Collectors.toSet());

			statusObserver.onConnecting();

			buffer.clear();
			buffer.add(Mode1CommandGroup.INIT);
			buffer.add(Mode1CommandGroup.SUPPORTED_PIDS);
			buffer.add(new InitCompletedCommand());
			
			log.info("Starting the workflow: {}. Selected PID's: {}", getClass().getSimpleName(), filter);

			final Mode1Producer producer = Mode1Producer
					.builder()
					.buffer(buffer)
					.batchEnabled(batchEnabled)
					.pidRegistry(pidRegistry)
					.policy(policy)
					.filter(newFilter).build();

			final CommandExecutor executor = CommandExecutor
					.builder()
					.connection(connection)
					.buffer(buffer)
					.subscribe(producer)
					.subscribe(subscriber)
					.policy(executorPolicy)
					.codecRegistry(codecRegistry)
					.statusObserver(statusObserver)
					.build();

			final ExecutorService executorService = Executors.newFixedThreadPool(2);

			try {
				executorService.invokeAll(Arrays.asList(executor, producer));
			} catch (InterruptedException e) {
				log.error("Failed to schedule workers.", e);
			} finally {
				statusObserver.onStopped();
				executorService.shutdown();
			}
		};

		taskPool.submit(task);
	}
}
