package org.obd.metrics.workflow;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.obd.metrics.CommandReplySubscriber;
import org.obd.metrics.CommandsBuffer;
import org.obd.metrics.ProducerPolicy;
import org.obd.metrics.command.CommandReply;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.QuitCommand;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
final class GenericProducer extends CommandReplySubscriber implements Callable<String> {

	private final CommandsBuffer buffer;

	private final ProducerPolicy policy;
	
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
			TimeUnit.MILLISECONDS.sleep(policy.getDelayBeforeInsertingCommands());
			if (cycleCommands.isEmpty()) {
				TimeUnit.MILLISECONDS.sleep(policy.getEmptyBufferSleepTime());
			} else {
				buffer.addAll(cycleCommands);
			}
		}
		log.info("Recieved QUIT command. Ending the process.");
		return null;
	}
}
