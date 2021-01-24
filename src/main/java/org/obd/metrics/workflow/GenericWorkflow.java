package org.obd.metrics.workflow;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.obd.metrics.CommandExecutor;
import org.obd.metrics.MetricsObserver;
import org.obd.metrics.StatusObserver;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.InitCompletedCommand;
import org.obd.metrics.connection.Connection;
import org.obd.metrics.pid.PidDefinition;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class GenericWorkflow extends Workflow {

	final EcuSpecific ecuSpecific;

	GenericWorkflow(EcuSpecific ecuSpecific, String equationEngine, MetricsObserver subscriber,
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
					.subscribe(metricsObserver)
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
