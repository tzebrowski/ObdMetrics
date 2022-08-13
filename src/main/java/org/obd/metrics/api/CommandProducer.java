package org.obd.metrics.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.DeviceProperties;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Lifecycle;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.context.Context;
import org.obd.metrics.diagnostic.Diagnostics;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class CommandProducer implements Callable<String>, Lifecycle {

	@SuppressWarnings("serial")
	private static final Map<Integer, Integer> PID_PRIORITY_TO_TICK = new HashMap<Integer, Integer>() {
		{
			put(0, 0);
			put(1, 5);
			put(2, 20);
			put(3, 50);
			put(4, 100);
			put(5, 200);
			put(6, 500);
			put(7, 1000);
			put(8, 2000);
			put(9, 5000);
			put(10, 10000);
		}
	};

	private static final int POLICY_MAX_ITEMS_IN_THE_BUFFER = 100;
	private final Supplier<List<ObdCommand>> commandsSupplier;
	private final AdaptiveTimeout adaptiveTimeout;
	private final Adjustments adjustements;
	private final CANMessageHeaderManager messageHeaderInjector;

	private volatile boolean isStopped = false;
	private volatile boolean isRunning = false;

	CommandProducer(Diagnostics dianostics, Supplier<List<ObdCommand>> commandsSupplier, Adjustments adjustements,
			Init init) {
		this.adjustements = adjustements;
		this.commandsSupplier = commandsSupplier;

		this.adaptiveTimeout = new AdaptiveTimeout(adjustements.getAdaptiveTiming(), dianostics);
		this.messageHeaderInjector = new CANMessageHeaderManager(init);
	}

	@Override
	public void onRunning(DeviceProperties properties) {
		log.info("Received onRunning event. Starting command producer thread.");
		isRunning = true;
	}

	@Override
	public void onStopping() {
		log.info("Received onStopping event. Stopping command pProducer thread.");
		isStopped = true;
	}

	@Override
	public String call() throws Exception {
		try {

			final Map<Integer, Integer> ticks = PID_PRIORITY_TO_TICK.keySet().stream()
					.collect(Collectors.toMap(i -> i, c -> 0));

			final ProducerPolicy producerPolicy = adjustements.getProducerPolicy();

			log.info("Starting command producer thread. Policy: {}.... ", producerPolicy.toString());

			final ConditionalSleep sleep = ConditionalSleep.builder().slice(20l).condition(() -> isStopped).build();

			adaptiveTimeout.schedule();

			final CommandsBuffer buffer = Context.instance().resolve(CommandsBuffer.class).get();

			while (!isStopped) {

				sleep.sleep(adaptiveTimeout.getCurrentTimeout());

				final List<ObdCommand> commands = commandsSupplier.get();

				if (isRunning) {
					messageHeaderInjector.testSingleMode(commands);

					if (adjustements.isBatchEnabled() && producerPolicy.isPriorityQueueEnabled()
							&& commands.size() > 1) {

						if (isBufferFull(buffer)) {
							log.trace("Command buffer is full. Skip adding to the buffer");
						} else {

							commands.stream().collect(Collectors.groupingBy(ObdCommand::getPriority))
								.forEach((priority, c) -> {
									final Integer tickThreshold = PID_PRIORITY_TO_TICK.get(priority);
									if (null == tickThreshold) {
										log.warn("No pririty configuration found for the PID: {}", priority);
									} else {
										
										int currentTick = ticks.get(priority);
										
										if (log.isTraceEnabled()) {
											log.trace("Priority group={}, currentTick={}, tickThreshold={}", priority, currentTick, tickThreshold);
										}
										
										if (tickThreshold == 0) {
											// always add highest priority to list
											addCommandsToTheBuffer(buffer, c);
										} else {
											if (currentTick == 0 ) {
												addCommandsToTheBuffer(buffer, c);
												ticks.put(priority, ++currentTick);
											} else if (currentTick == tickThreshold) {
												addCommandsToTheBuffer(buffer, c);
												ticks.put(priority, 0);
											} else {
												ticks.put(priority, ++currentTick);
											}
										}
									}
								});
						}
					} else {
						
						if (log.isTraceEnabled()) {
							log.trace("Priority queue is disabled. Adding all commands to the buffer: {}", commands);
						}
						
						addCommandsToTheBuffer(buffer, commands);
					}
				} else {
					log.trace("No commands are provided by supplier yet");
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			adaptiveTimeout.cancel();
			log.info("Completed producer thread.");
		}
		return null;
	}

	private boolean isBufferFull(final CommandsBuffer buffer) {
		return buffer.size() >= POLICY_MAX_ITEMS_IN_THE_BUFFER;
	}

	private void addCommandsToTheBuffer(final CommandsBuffer buffer, final List<ObdCommand> commands) {
		if (log.isTraceEnabled()) {
			log.trace("Adding commands to the queue: {}", commands);
		}
		
		commands.forEach(command -> {
			messageHeaderInjector.switchHeader(command);
			buffer.addLast(command);
		});
	}
}
