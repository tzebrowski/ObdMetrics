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
			log.info("Publisher. Recieved QUIT command.");
			quit = true;
		}
	}

	@Override
	public String call() throws Exception {
		log.info("Staring publishing thread....");
		try {
			while (!quit) {
				sleep(policy.getDelayBeforeInsertingCommands());
				if (cycleCommands.isEmpty()) {
					TimeUnit.MILLISECONDS.sleep(policy.getEmptyBufferSleepTime());
				} else {
					buffer.addAll(cycleCommands);
				}
			}
		} finally {
			log.info("Completed Publisher.");
		}
		return null;
	}

	long sleep(final long timeout) {
		if (waitTime >= timeout) {
			try {
				TimeUnit.MILLISECONDS.sleep(timeout);
			} catch (InterruptedException e) {
			}
			return timeout;
		} else {
			long start = System.currentTimeMillis();
			long cnt = 0;
			do {
				try {
					TimeUnit.MILLISECONDS.sleep(waitTime);
				} catch (InterruptedException e) {
				}
				cnt = System.currentTimeMillis() - start;
			} while (cnt < (timeout - 9) && !quit);
			return cnt;
		}
	}
}
