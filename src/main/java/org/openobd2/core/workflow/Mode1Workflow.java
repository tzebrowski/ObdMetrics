package org.openobd2.core.workflow;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openobd2.core.CommandExecutor;
import org.openobd2.core.CommandReplySubscriber;
import org.openobd2.core.StatusObserver;
import org.openobd2.core.command.group.Mode1CommandGroup;
import org.openobd2.core.command.process.InitCompletedCommand;
import org.openobd2.core.connection.Connection;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class Mode1Workflow extends WorkflowBase {

	private final boolean batchEnabled;
	
	Mode1Workflow(String equationEngine, CommandReplySubscriber subscriber, StatusObserver state, boolean batchEnabled) throws IOException {
		super(equationEngine, subscriber, state, "mode01.json");
		this.batchEnabled = batchEnabled;
	}

	@Override
	public void start(Connection connection, Set<String> pids) {
		final Runnable task = () -> {

			statusObserver.onConnecting();

			buffer.clear();
			buffer.add(Mode1CommandGroup.INIT);
			buffer.add(Mode1CommandGroup.SUPPORTED_PIDS);
			buffer.add(new InitCompletedCommand());
			
			log.info("Starting the workflow: {}. Selected PID's: {}", getClass().getSimpleName(), pids);

			final Mode1Producer producer = Mode1Producer
					.builder()
					.buffer(buffer)
					.batchEnabled(batchEnabled)
					.pidRegistry(pidRegistry)
					.policy(policy)
					.selectedPids(pids).build();

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
