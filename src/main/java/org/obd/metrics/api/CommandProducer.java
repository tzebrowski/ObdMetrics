package org.obd.metrics.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.context.Context;
import org.obd.metrics.diagnostic.Diagnostics;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class CommandProducer extends LifecycleAdapter implements Callable<Void>{

	private static final int POLICY_MAX_COMMANDS_IN_THE_BUFFER = 100;
	private final Supplier<List<ObdCommand>> commandsSupplier;
	private final AdaptiveTimeout adaptiveTimeout;
	private final Adjustments adjustements;
	private final CANMessageHeaderManager messageHeaderManager;

	CommandProducer(Diagnostics dianostics, Supplier<List<ObdCommand>> commandsSupplier, Adjustments adjustements,
			Init init) {
		this.adjustements = adjustements;
		this.commandsSupplier = commandsSupplier;

		this.adaptiveTimeout = new AdaptiveTimeout(adjustements.getAdaptiveTimeoutPolicy(), dianostics);
		this.messageHeaderManager = new CANMessageHeaderManager(init);
	}


	@Override
	public Void call() throws Exception {
		try {

			final ProducerPolicy producerPolicy = adjustements.getProducerPolicy();
			
			final Map<Integer, Integer> commandsPriorities = getCommandsPriorities(producerPolicy);
			
			final Map<Integer, Integer> ticks = commandsPriorities.keySet().stream()
					.collect(Collectors.toMap(i -> i, c -> 0));

			log.info("Starting command producer thread. Priorities: {} ", commandsPriorities);

			final ConditionalSleep sleep = ConditionalSleep
					.builder()
					.enabled(producerPolicy.getConditionalSleepEnabled())
					.slice(producerPolicy.getConditionalSleepSliceSize())
					.condition(() -> isStopped)
					.build();

			adaptiveTimeout.schedule();

			final CommandsBuffer buffer = Context.instance().resolve(CommandsBuffer.class).get();

			while (!isStopped) {

				sleep.sleep(adaptiveTimeout.getCurrentTimeout());

				final List<ObdCommand> commands = commandsSupplier.get();

				if (isRunning) {
					messageHeaderManager.testSingleMode(commands);

					if (adjustements.getBatchPolicy().isEnabled() && producerPolicy.isPriorityQueueEnabled()
							&& commands.size() > 1) {

						if (isBufferFull(buffer)) {
							log.trace("Command buffer is full. Skip adding to the buffer");
						} else {

							commands.stream().collect(Collectors.groupingBy(ObdCommand::getPriority))
								.forEach((priority, c) -> {
									final Integer tickThreshold = commandsPriorities.get(priority);
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
		} finally {
			adaptiveTimeout.cancel();
			log.info("Completed producer thread.");
		}
		return null;
	}

	private Map<Integer, Integer> getCommandsPriorities(final ProducerPolicy producerPolicy) {
		final Map<Integer, Integer> pidPriority = new HashMap<>(ProducerPolicy.DEFAULT_COMMAND_PRIORITY);
		pidPriority.putAll(producerPolicy.getPidPriorities()); //overrides defaults
		return pidPriority;
	}

	private boolean isBufferFull(final CommandsBuffer buffer) {
		return buffer.size() >= POLICY_MAX_COMMANDS_IN_THE_BUFFER;
	}

	private void addCommandsToTheBuffer(final CommandsBuffer buffer, final List<ObdCommand> commands) {
		if (log.isTraceEnabled()) {
			log.trace("Adding commands to the queue: {}", commands);
		}
	
		commands.stream().forEach(command -> {
			if (!adjustements.getStNxx().isEnabled()) {
				messageHeaderManager.switchHeader(command);
			}
			buffer.addLast(command);
		});
	}
}
