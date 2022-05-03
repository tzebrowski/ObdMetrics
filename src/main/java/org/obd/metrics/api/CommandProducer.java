package org.obd.metrics.api;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.obd.metrics.DeviceProperties;
import org.obd.metrics.Lifecycle;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.command.obd.BatchObdCommand;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.diagnostic.Diagnostics;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class CommandProducer implements Callable<String>, Lifecycle {
	
	private static final int POLICY_MAX_ITEMS_IN_THE_QUEUE = 100;
	private final CommandsBuffer buffer;
	private final Supplier<List<ObdCommand>> commandsSupplier;
	private final AdaptiveTimeout adaptiveTimeout;
	private final Adjustments adjustements;
	private int addToQueueCnt = 0;
	private final CANMessageHeaderInjector messageHeaderInjector;

	private volatile boolean isStopped = false;
	private volatile boolean isRunning = false;

	CommandProducer(Diagnostics dianostics, CommandsBuffer buffer, Supplier<List<ObdCommand>> commandsSupplier,
			Adjustments adjustements, Init init) {
		this.adjustements = adjustements;
		this.commandsSupplier = commandsSupplier;
		this.buffer = buffer;
		this.adaptiveTimeout = new AdaptiveTimeout(adjustements.getAdaptiveTiming(), dianostics);
		this.messageHeaderInjector = new CANMessageHeaderInjector(buffer, init);
	}

	@Override
	public void onRunning(DeviceProperties properties) {
		log.info("Received onRunning event. Start Command Producer thread.");
		isRunning = true;
	}

	@Override
	public void onStopping() {
		log.info("Received onStopping event. Stopping Command Producer thread.");
		isStopped = true;
	}

	@Override
	public String call() throws Exception {
		try {

			final ProducerPolicy producerPolicy = adjustements.getProducerPolicy();

			log.info("Starting Producer thread. Policy: {}.... ", producerPolicy.toString());

			final ConditionalSleep sleep = ConditionalSleep.builder().slice(20l).condition(() -> isStopped).build();

			adaptiveTimeout.schedule();

			while (!isStopped) {

				final long currentTimeout = adaptiveTimeout.getCurrentTimeout();
				sleep.sleep(currentTimeout);

				final List<ObdCommand> commands = commandsSupplier.get();

				if (isRunning) {
					messageHeaderInjector.testSingleMode(commands);

					if (adjustements.isBatchEnabled() && producerPolicy.isPriorityQueueEnabled()
							&& commands.size() > 1) {
						// every X ms we add all the commands
						final long threshold = producerPolicy.getLowPriorityCommandFrequencyDelay() / currentTimeout;
						log.trace("Priority queue is enabled. Current counter: {}, threshold: {}", addToQueueCnt,
								threshold);
						if (buffer.size() < POLICY_MAX_ITEMS_IN_THE_QUEUE) {
							if (addToQueueCnt >= threshold) {
								log.trace("Adding low priority commands to the buffer: {}", commands);
								addCommandsToTheQueue(commands);
								addToQueueCnt = 0;
							} else {
								// add just high priority commands
								final List<ObdCommand> filteredByPriority = commands.stream()
										.filter(filterByPriority(0)).map(p -> p).collect(Collectors.toList());

								log.trace("Adding high priority commands to the buffer: {}", filteredByPriority);
								addCommandsToTheQueue(filteredByPriority);
							}
							addToQueueCnt++;
						} else {
							addToQueueCnt++;
							log.trace("Skip adding to the buffer");
						}
					} else {
						log.trace("Priority queue is disabled. Adding all commands to the buffer: {}", commands);
						buffer.addAll(commands);
					}
				} else {
					log.trace("No commands are provided by supplier yet");
				}
			}
		} finally {
			adaptiveTimeout.cancel();
			log.info("Completed producer thread.");
		}
		return null;
	}

	private void addCommandsToTheQueue(final List<ObdCommand> commands) {
		commands.forEach(command -> {
			messageHeaderInjector.switchHeader(command);
			buffer.addLast(command);
		});
	}

	private Predicate<? super ObdCommand> filterByPriority(int priority) {
		return p -> (p instanceof BatchObdCommand) && ((BatchObdCommand) p).getPriority() == priority;
	}
}
