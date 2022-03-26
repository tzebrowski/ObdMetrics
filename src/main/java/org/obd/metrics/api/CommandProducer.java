package org.obd.metrics.api;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.obd.metrics.Reply;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.command.obd.BatchObdCommand;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.diagnostic.Diagnostics;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class CommandProducer extends ReplyObserver<Reply<?>> implements Callable<String> {

	private final CommandsBuffer buffer;
	private final CommandsSuplier commandsSupplier;
	private final AdaptiveTimeout adaptiveTimeout;
	private final Adjustments adjustements;
	private volatile boolean quit = false;
	private int addCnt = 0;

	CommandProducer(
	        Diagnostics dianostics,
	        CommandsBuffer buffer,
	        CommandsSuplier commandsSupplier,
	        Adjustments adjustements) {
		this.adjustements = adjustements;
		this.commandsSupplier = commandsSupplier;
		this.buffer = buffer;
		this.adaptiveTimeout = new AdaptiveTimeout(adjustements.getAdaptiveTiming(), dianostics);
	}

	@Override
	public void onNext(Reply<?> reply) {
		log.trace("Received command reply: {}", reply);

		if (reply.getCommand() instanceof QuitCommand) {
			log.debug("Received QUIT command. Shutdowning Comand Producer");
			quit = true;
		}
	}

	@Override
	public List<Class<?>> subscribeFor() {
		return Arrays.asList(QuitCommand.class);
	}

	@Override
	public String call() throws Exception {
		try {

			final ProducerPolicy producerPolicy = adjustements.getProducerPolicy();

			log.info("Starting Producer thread. Policy: {}.... ", producerPolicy.toString());

			final Throttle throttle = Throttle
			        .builder()
			        .slice(20l)
			        .condition(() -> quit)
			        .build();

			adaptiveTimeout.schedule();

			while (!quit) {
				final long currentTimeout = adaptiveTimeout.getCurrentTimeout();
				throttle.sleep(currentTimeout);
				commandsSupplier.get().ifPresent(commands -> {
					if (adjustements.isBatchEnabled() && producerPolicy.isPriorityQueueEnabled()
					        && commands.size() > 1) {
						// every X ms we add all the commands
						final long threshold = producerPolicy.getLowPriorityCommandFrequencyDelay() / currentTimeout;
						log.trace("Priority queue is enabled. Current counter: {}, threshold: {}", addCnt, threshold);
						if (addCnt >= threshold) {
							log.trace("Adding low priority commands to the buffer: {}", commands);
							buffer.addAll(commands);
							addCnt = 0;
						} else {
							// add just high priority commands
							final List<ObdCommand> filteredByPriority = commands.stream().filter(
							        filterByPriority(0))
							        .map(p -> p)
							        .collect(Collectors.toList());

							log.trace("Adding high priority commands to the buffer: {}", filteredByPriority);
							filteredByPriority.forEach(buffer::addLast);
							addCnt++;
						}
					} else {
						log.trace("Priority queue is disabled. Adding all commands to the buffer: {}", commands);
						buffer.addAll(commands);
					}
				});
			}
		} finally {
			adaptiveTimeout.cancel();
			log.info("Completed Producer thread.");
		}
		return null;
	}

	private Predicate<? super ObdCommand> filterByPriority(int priority) {
		return p -> (p instanceof BatchObdCommand) && ((BatchObdCommand) p).getPriority() == priority;
	}
}
