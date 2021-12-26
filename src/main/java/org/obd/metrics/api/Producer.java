package org.obd.metrics.api;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import org.obd.metrics.Reply;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.statistics.StatisticsRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class Producer extends ReplyObserver<Reply<?>> implements Callable<String> {

	private final CommandsBuffer buffer;
	private final Supplier<Optional<Collection<ObdCommand>>> commandsSupplier;
	private final AdaptiveTimeout adaptiveTimeout;
	private final Adjustments adjustements;
	private volatile boolean quit = false;
	private int addCnt = 0;

	Producer(StatisticsRegistry statisticsRegistry,
	        CommandsBuffer buffer,
	        Supplier<Optional<Collection<ObdCommand>>> commandsSupplier,
	        Adjustments adjustements) {
		this.adjustements = adjustements;
		this.commandsSupplier = commandsSupplier;
		this.buffer = buffer;
		this.adaptiveTimeout = new AdaptiveTimeout(adjustements.getAdaptiveTiming(), statisticsRegistry);
	}

	@Override
	public void onNext(Reply<?> reply) {
		log.trace("Recieve command reply: {}", reply);

		if (reply.getCommand() instanceof QuitCommand) {
			log.debug("Producer. Recieved QUIT command.");
			quit = true;
		}
	}

	@Override
	public String[] observables() {
		return new String[] { QuitCommand.class.getName() };
	}

	@Override
	public String call() throws Exception {
		try {

			final ProducerPolicy producerPolicy = adjustements.getProducerPolicy();

			log.info("Starting Producer thread. Policy: {}.... ", producerPolicy.toString());

			final ConditionalSleep conditionalSleep = ConditionalSleep
			        .builder()
			        .slice(20l)
			        .condition(() -> quit)
			        .build();

			adaptiveTimeout.schedule();

			while (!quit) {
				final long currentTimeout = adaptiveTimeout.getCurrentTimeout();
				conditionalSleep.sleep(currentTimeout);
				commandsSupplier.get().ifPresent(commands -> {
					if (adjustements.isBatchEnabled() && producerPolicy.isPriorityQueueEnabled()
					        && commands.size() > 1) {
						// every X ms we add all the commands
						if (addCnt >= (producerPolicy.getLowPriorityCommandFrequencyDelay() / currentTimeout)) {
							buffer.addAll(commands);
							addCnt = 0;
						} else {
							// add just high priority commands
							// always first command
							buffer.addLast(commands.iterator().next());
							addCnt++;
						}
					} else {
						buffer.addAll(commands);
						log.trace("Adding commands to the buffer: {}", commands);
					}
				});
			}

		} finally {
			adaptiveTimeout.cancel();
			log.info("Completed Producer thread.");
		}
		return null;
	}
}
