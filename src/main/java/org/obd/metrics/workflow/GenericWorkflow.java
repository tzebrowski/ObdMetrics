package org.obd.metrics.workflow;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.obd.metrics.CommandExecutor;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.InitCompletedCommand;
import org.obd.metrics.connection.Connection;
import org.obd.metrics.pid.PidDefinition;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class GenericWorkflow extends Workflow {

	final EcuSpecific ecuSpecific;

	GenericWorkflow(EcuSpecific ecuSpecific) throws IOException {
		super(ecuSpecific.getPidFile());
		this.ecuSpecific = ecuSpecific;
	}

	@Override
	public void start(Connection connection, Set<String> filter, boolean batchEnabled) {
		final Runnable task = () -> {

			final Set<String> newFilter = filter == null ? Collections.emptySet()
					: filter.stream().map(p -> p.toLowerCase()).collect(Collectors.toSet());

			status.onConnecting();
			comandsBuffer.clear();
			comandsBuffer.add(ecuSpecific.getInitSequence());
			comandsBuffer.add(new InitCompletedCommand());

			final Set<ObdCommand> cycleCommands = newFilter.stream().map(pid -> {
				final PidDefinition pidDefinition = pids.findBy(pid);
				if (pidDefinition == null) {
					log.warn("No pid definition found for pid: {}", pid);
					return null;
				} else {
					return new ObdCommand(pidDefinition);
				}
			}).filter(p -> p != null).collect(Collectors.toSet());

			log.info("Starting the workflow: {}. Selected PID's: {}", getClass().getSimpleName(), cycleCommands);

			var producer = GenericProducer.builder().buffer(comandsBuffer).policy(producerPolicy)
					.cycleCommands(cycleCommands).build();

			var executor = CommandExecutor.builder().connection(connection).buffer(comandsBuffer).subscribe(producer)
					.subscribe(metricsObserver).subscribe(statistics).policy(executorPolicy).statusObserver(status)
					.codecRegistry(codec).build();

			var executorService = Executors.newFixedThreadPool(2);

			try {
				executorService.invokeAll(Arrays.asList(executor, producer));
				log.info("Completed all the tasks.");
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
