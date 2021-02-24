package org.obd.metrics.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.obd.metrics.CommandLoop;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.Lifecycle;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.InitCompletedCommand;
import org.obd.metrics.pid.PidDefinition;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class GenericWorkflow extends AbstractWorkflow {

	GenericWorkflow(@NonNull EcuSpecific ecuSpecific, String equationEngine, @NonNull ReplyObserver observer,
	        Lifecycle lifecycle, Long commandFrequency, GeneratorSpec generator) throws IOException {
		super(ecuSpecific, equationEngine, observer, lifecycle, commandFrequency, generator);
	}

	@Override
	public void start(@NonNull WorkflowContext ctx) {
		final Runnable task = () -> {

			lifecycle.onConnecting();
			comandsBuffer.clear();
			ecuSpecific.getSequences().forEach(comandsBuffer::add);
			comandsBuffer.add(new InitCompletedCommand());

			final Set<ObdCommand> cycleCommands = getCycleCommands(ctx);

			log.info("Starting the workflow: {}. Selected PID's: {}", getClass().getSimpleName(), cycleCommands);

			var producer = new Producer(comandsBuffer, producerPolicy, cycleCommands);

			var executor = CommandLoop.builder()
					.connection(ctx.connection)
					.buffer(comandsBuffer)
					.observer(producer)
					.observer(replyObserver)
					.observer(statistics)
					.pids(pids)
					.policy(executorPolicy).lifecycle(lifecycle)
					.codecRegistry(codec).build();

			var executorService = Executors.newFixedThreadPool(2);

			try {
				executorService.invokeAll(Arrays.asList(executor, producer));
				log.info("Completed all the tasks.");
			} catch (InterruptedException e) {
				log.error("Failed to schedule workers.", e);
			} finally {
				lifecycle.onStopped();
				executorService.shutdown();
			}
		};

		singleTaskPool.submit(task);
	}

	private Set<ObdCommand> getCycleCommands(WorkflowContext ctx) {
		final Set<Long> newFilter = ctx.filter == null ?  Collections.emptySet() : ctx.filter;

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
