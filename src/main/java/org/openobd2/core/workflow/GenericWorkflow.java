package org.openobd2.core.workflow;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.openobd2.core.CommandExecutor;
import org.openobd2.core.CommandReplySubscriber;
import org.openobd2.core.StatusObserver;
import org.openobd2.core.command.obd.ObdCommand;
import org.openobd2.core.command.process.InitCompletedCommand;
import org.openobd2.core.connection.Connection;
import org.openobd2.core.pid.PidDefinition;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class GenericWorkflow extends Workflow {

	final EcuSpecific ecuSpecific;

	GenericWorkflow(EcuSpecific ecuSpecific, String equationEngine, CommandReplySubscriber subscriber,
			StatusObserver statusObserver) throws IOException {
		super(equationEngine, subscriber, statusObserver, ecuSpecific.getPidFile());
		this.ecuSpecific = ecuSpecific;
	}

	@Override
	public void start(Connection connection,Set<String> filter, boolean batchEnabled) {
		final Runnable task = () -> {
				
			final Set<String> newFilter = filter == null ? Collections.emptySet() : filter.stream().map(p-> p.toLowerCase() ).collect(Collectors.toSet());

			
			statusObserver.onConnecting();
			buffer.clear();
			buffer.add(ecuSpecific.getInitSequence());
			buffer.add(new InitCompletedCommand());
			
			final Set<ObdCommand> cycleCommands = newFilter.stream().map(pid -> {
				final PidDefinition pidDefinition = pidRegistry.findBy(pid);
				if (pidDefinition == null) {
					log.warn("No pid definition found for pid: {}", pid);
					return null;
				} else {
					return new ObdCommand(pidDefinition);
				}
			}).filter(p->p!=null).collect(Collectors.toSet());

			log.info("Starting the workflow: {}. Selected PID's: {}", getClass().getSimpleName(),cycleCommands);
			
			final GenericProducer producer = GenericProducer
					.builder()
					.buffer(buffer)
					.policy(policy)
					.cycleCommands(cycleCommands)
					.build();

			final CommandExecutor executor = CommandExecutor
					.builder()
					.connection(connection)
					.buffer(buffer)
					.subscribe(producer)
					.subscribe(subscriber)
					.policy(executorPolicy)
					.statusObserver(statusObserver)
					.codecRegistry(codecRegistry)
					.build();

			final ExecutorService executorService = Executors.newFixedThreadPool(2);

			try {
				executorService.invokeAll(Arrays.asList(executor, producer));
				log.info("Completed all the tasks.");
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
