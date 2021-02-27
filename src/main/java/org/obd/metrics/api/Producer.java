package org.obd.metrics.api;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.obd.metrics.CommandsBuffer;
import org.obd.metrics.ProducerPolicy;
import org.obd.metrics.Reply;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.QuitCommand;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class Producer extends ReplyObserver implements Callable<String> {

	@NonNull
	protected CommandsBuffer buffer;

	@NonNull
	protected ProducerPolicy policy;

	@NonNull
	protected Collection<ObdCommand> cycleCommands;

	protected volatile boolean quit = false;

	@Override
	public void onNext(Reply<?> reply) {
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
