package org.obd.metrics.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.obd.metrics.CommandLoop;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.StatusObserver;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.InitCompletedCommand;
import org.obd.metrics.connection.Connection;
import org.obd.metrics.pid.PidDefinition;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class GenericWorkflow extends AbstractWorkflow {

	GenericWorkflow(@NonNull EcuSpecific ecuSpecific, String equationEngine, @NonNull ReplyObserver observer,
	        StatusObserver statusObserver, boolean enableGenerator, Double generatorIncrement, Long commandFrequency)
	        throws IOException {
		super(ecuSpecific, equationEngine, observer, statusObserver, enableGenerator, generatorIncrement,
		        commandFrequency);
	}

	@Override
	public void start(@NonNull Connection connection) {
		final Runnable task = () -> {

			status.onConnecting();
			comandsBuffer.clear();
			comandsBuffer.add(ecuSpecific.getInitSequence());
			comandsBuffer.add(new InitCompletedCommand());

			final Set<ObdCommand> cycleCommands = getCycleCommands();

			log.info("Starting the workflow: {}. Selected PID's: {}", getClass().getSimpleName(), cycleCommands);

			var producer = new Producer(comandsBuffer, producerPolicy, cycleCommands);

			var executor = CommandLoop.builder()
					.connection(connection)
					.buffer(comandsBuffer)
					.observer(producer)
					.observer(replyObserver)
					.observer(statistics)
					.pids(pids)
					.policy(executorPolicy).statusObserver(status)
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

	private Set<ObdCommand> getCycleCommands() {
		final Set<Long> newFilter = filter == null ?  Collections.emptySet() : filter;

		final Set<ObdCommand> cycleCommands = newFilter.stream().map(pid -> {
			final PidDefinition pidDefinition = pids.findBy(pid);
			if (pidDefinition == null) {
				log.warn("No pid definition found for pid: {}", pid);
				return null;
			} else {
				return new ObdCommand(pidDefinition);
			}
		}).filter(p -> p != null).collect(Collectors.toSet());
		return cycleCommands;
	}
}
