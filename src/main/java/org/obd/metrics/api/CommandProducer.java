/** 
 * Copyright 2019-2023, Tomasz Żebrowski
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
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
import org.obd.metrics.context.Service;
import org.obd.metrics.diagnostic.Diagnostics;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class CommandProducer extends LifecycleAdapter implements Callable<Void>, Service {

	private static final int POLICY_MAX_COMMANDS_IN_THE_BUFFER = 100;
	
	
	private AdaptiveTimeout adaptiveTimeout;
	
	private Adjustments adjustments;

	private final CANMessageHeaderManager messageHeaderManager;

	private transient Map<Integer, Integer> commandsPriorities;
	
	private transient Map<Integer, Integer> ticks;
	
	private transient ConditionalSleep sleep;

	private transient Supplier<List<ObdCommand>> commandsSupplier;

	CommandProducer(Diagnostics dianostics, Supplier<List<ObdCommand>> commandsSupplier, Adjustments adjustements,
			Init init) {
		this.adjustments = adjustements;
		this.commandsSupplier = commandsSupplier;

		this.adaptiveTimeout = new AdaptiveTimeout(adjustements.getAdaptiveTimeoutPolicy(), dianostics);
		this.messageHeaderManager = new CANMessageHeaderManager(init);
	}
	
	void pause() {
		isRunning = false;
		adaptiveTimeout.cancel();
	}
	
	void resume() {
		adaptiveTimeout.schedule();
		isRunning = true;
	}
	
	void updateSettings(Adjustments adjustments, Supplier<List<ObdCommand>> commandsSuplier, Diagnostics dianostics) {
		final ProducerPolicy producerPolicy = adjustments.getProducerPolicy();
		
		this.commandsSupplier = commandsSuplier;
		this.commandsPriorities = getCommandsPriorities(producerPolicy);
		this.adaptiveTimeout = new AdaptiveTimeout(adjustments.getAdaptiveTimeoutPolicy(), dianostics);
		this.ticks = commandsPriorities.keySet().stream()
				.collect(Collectors.toMap(i -> i, c -> 0));
		
		this.adjustments = adjustments;
		
		log.info("Starting command producer thread. Priorities: {} ", commandsPriorities);

		this.sleep = ConditionalSleep
				.builder()
				.enabled(producerPolicy.getConditionalSleepEnabled())
				.slice(producerPolicy.getConditionalSleepSliceSize())
				.condition(() -> isStopped)
				.build();

	}
	
	
	@Override
	public Void call() throws Exception {
		try {

			final ProducerPolicy producerPolicy = adjustments.getProducerPolicy();
			
			commandsPriorities = getCommandsPriorities(producerPolicy);
			
			ticks = commandsPriorities.keySet().stream()
					.collect(Collectors.toMap(i -> i, c -> 0));

			log.info("Starting command producer thread. Priorities: {} ", commandsPriorities);

			sleep = ConditionalSleep
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
					messageHeaderManager.testIfSingleService(commands);

					if (adjustments.getBatchPolicy().isEnabled() && producerPolicy.isPriorityQueueEnabled()
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
			if (!adjustments.getStNxx().isEnabled()) {
				messageHeaderManager.switchHeader(command);
			}
			buffer.addLast(command);
		});
	}
}
