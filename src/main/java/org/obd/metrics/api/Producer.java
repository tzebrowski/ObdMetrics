package org.obd.metrics.api;

import java.util.Collection;
import java.util.concurrent.Callable;

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

	static long waitTime = 20;

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
			log.debug("Publisher. Recieved QUIT command.");
			quit = true;
		}
	}

	@Override
	public String call() throws Exception {
		log.info("Starting Publisher thread....");

		var beforeFeelingQueue = ConditionalSleep
		        .builder()
		        .sleepTime(20l)
		        .condition(() -> quit)
		        .build();

		try {
			while (!quit) {

				beforeFeelingQueue.sleep(policy.getBeforeFeelingQueue());
				buffer.addAll(cycleCommands);
			}
		} finally {
			log.info("Completed Publisher thread.");
		}
		return null;
	}
}
