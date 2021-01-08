package org.openobd2.core.workflow;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.openobd2.core.CommandReplySubscriber;
import org.openobd2.core.CommandsBuffer;
import org.openobd2.core.ProducerPolicy;
import org.openobd2.core.command.CommandReply;
import org.openobd2.core.command.obd.ObdCommand;
import org.openobd2.core.command.process.QuitCommand;
import org.openobd2.core.pid.PidRegistry;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
final class Mode22CommandsProducer extends CommandReplySubscriber implements Callable<String> {

	private final CommandsBuffer buffer;

	private final ProducerPolicy policy;

	@NonNull
	private final PidRegistry pidDefinitionRegistry;

	@NonNull
	private final Set<ObdCommand> cycleCommands;

	@Default
	private volatile boolean quit = false;

	@Override
	public void onNext(CommandReply<?> reply) {
		log.trace("Recieve command reply: {}", reply);
		if (reply.getCommand() instanceof QuitCommand) {
			quit = true;
		}
	}

	@Override
	public String call() throws Exception {
		log.info("Staring publishing thread....");

		while (!quit) {
			TimeUnit.MILLISECONDS.sleep(policy.getFrequency());
			if (cycleCommands.isEmpty()) {
				TimeUnit.MILLISECONDS.sleep(100);
			} else {
				buffer.addAll(cycleCommands);
			}
		}
		log.info("Recieved QUIT command. Ending the process.");
		return null;
	}
}
