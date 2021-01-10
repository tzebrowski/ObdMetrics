package org.openobd2.core.workflow;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.openobd2.core.CommandExecutor;
import org.openobd2.core.CommandReplySubscriber;
import org.openobd2.core.StatusListener;
import org.openobd2.core.command.group.AlfaMed17CommandGroup;
import org.openobd2.core.command.obd.ObdCommand;
import org.openobd2.core.command.process.QuitCommand;
import org.openobd2.core.connection.Connection;
import org.openobd2.core.pid.PidDefinition;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class Mode22Workflow extends WorkflowBase {

	Mode22Workflow(String equationEngine, CommandReplySubscriber subscriber, StatusListener state) throws IOException{
		super (equationEngine,subscriber,state,"alfa.json");
	}

	@Override
	public void start(Connection connection,Set<String> selectedPids) {
		final Runnable task = () -> {
			
			state.onConnecting();
			buffer.clear();
			buffer.add(AlfaMed17CommandGroup.CAN_INIT);

			final Set<ObdCommand> cycleCommands = selectedPids.stream().map(pid -> {
				final PidDefinition pidDefinition = pidRegistry.findBy("22", pid);
				if (pidDefinition == null) {
					log.warn("No pid definition found for pid: {}", pid);
					return null;
				} else {
					return new ObdCommand(pidDefinition);
				}
			}).filter(p->p!=null).collect(Collectors.toSet());

			log.info("Starting the workflow: {}. Selected PID's: {}", getClass().getSimpleName(),cycleCommands);
			
			final Mode22Producer producer = Mode22Producer
					.builder().buffer(buffer)
					.pidDefinitionRegistry(pidRegistry).policy(policy).cycleCommands(cycleCommands).build();

			final CommandExecutor executor = CommandExecutor
					.builder()
					.connection(connection)
					.buffer(buffer)
					.subscribe(producer)
					.subscribe(subscriber)
					.policy(executorPolicy)
					.state(state)
					.codecRegistry(codecRegistry)
					.build();

			final ExecutorService executorService = Executors.newFixedThreadPool(2);

			try {
				executorService.invokeAll(Arrays.asList(executor, producer));
			} catch (InterruptedException e) {
				log.error("Failed to schedule workers.", e);
			} finally {
				state.onComplete();
				executorService.shutdown();
			}
		};

		taskPool.submit(task);
	}

	@Override
	public void stop() {
		log.info("Stopping the workflow: {}", getClass().getSimpleName());
		buffer.addFirst(new QuitCommand());
		state.onStopping();
	}
}
