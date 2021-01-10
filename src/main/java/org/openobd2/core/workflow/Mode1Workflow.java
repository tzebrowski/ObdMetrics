package org.openobd2.core.workflow;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openobd2.core.CommandExecutor;
import org.openobd2.core.CommandReplySubscriber;
import org.openobd2.core.StatusListener;
import org.openobd2.core.command.group.Mode1CommandGroup;
import org.openobd2.core.command.process.QuitCommand;
import org.openobd2.core.connection.Connection;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class Mode1Workflow extends WorkflowBase{
	
	Mode1Workflow(String equationEngine, CommandReplySubscriber subscriber, StatusListener state) throws IOException{
		super (equationEngine,subscriber,state,"generic.json");
	}
	
	@Override
	public void start(Connection connection,Set<String> selectedPids) {
		final Runnable task = () -> {
			
			state.onConnecting();

			buffer.clear();
			buffer.add(Mode1CommandGroup.INIT_PROTO_DEFAULT);
			buffer.add(Mode1CommandGroup.SUPPORTED_PIDS);
	        
			log.info("Starting the workflow: {}. Selected PID's: {}", getClass().getSimpleName(),selectedPids);

			final Mode1Producer producer = Mode1Producer
					.builder()
					.buffer(buffer)
					.pidDefinitionRegistry(pidRegistry)
					.policy(policy)
					.selectedPids(selectedPids)
					.build();

			final CommandExecutor executor = CommandExecutor.builder().connection(connection).buffer(buffer)
					.subscribe(producer)
					.subscribe(subscriber)
					.policy(executorPolicy)
					.codecRegistry(codecRegistry)
					.state(state)
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
