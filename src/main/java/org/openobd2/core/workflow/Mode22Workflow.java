package org.openobd2.core.workflow;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.openobd2.core.CommandExecutor;
import org.openobd2.core.CommandReplySubscriber;
import org.openobd2.core.CommandsBuffer;
import org.openobd2.core.ExecutorPolicy;
import org.openobd2.core.ProducerPolicy;
import org.openobd2.core.codec.CodecRegistry;
import org.openobd2.core.command.group.AlfaMed17CommandGroup;
import org.openobd2.core.command.obd.ObdCommand;
import org.openobd2.core.command.process.QuitCommand;
import org.openobd2.core.connection.Connection;
import org.openobd2.core.pid.PidDefinition;
import org.openobd2.core.pid.PidRegistry;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class Mode22Workflow implements Workflow {

	private final CommandsBuffer buffer = CommandsBuffer.instance(); 
	private final ProducerPolicy policy = ProducerPolicy.builder().frequency(50).build();
	private final ExecutorPolicy executorPolicy = ExecutorPolicy.builder().frequency(100).build();

	// just a single thread in a pool
	private static ExecutorService taskPool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(1), new ThreadPoolExecutor.DiscardPolicy());

	@NonNull
	private final PidRegistry pidRegistry;

	@NonNull
	private final CodecRegistry codecRegistry;
	
	@NonNull
	private final CommandReplySubscriber subscriber;

	@NonNull
	private final State state;
	
	
	@Builder
	static Mode22Workflow build (String equationEngine, CommandReplySubscriber subscriber, State state) throws IOException {
		
		try(final InputStream source = Thread.currentThread().getContextClassLoader().getResourceAsStream("alfa.json")){
			final State stateListener = state == null ? State.DUMMY : state;
			final PidRegistry pids = PidRegistry.builder().source(source).build();
			final Mode22Workflow workflow = new Mode22Workflow(
					pids, 
					CodecRegistry.builder().equationEngine(equationEngine).pids(pids).build(), 
					subscriber, 
					stateListener);
			return workflow;
		}
	}

	@Override
	public void start(Connection connection,Set<String> selectedPids) {
		final Runnable task = () -> {
			
			state.onStarting();
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

			final CommandExecutor executor = CommandExecutor.builder().connection(connection).buffer(buffer)
					.subscribe(producer).subscribe(subscriber).policy(executorPolicy).codecRegistry(codecRegistry)
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
