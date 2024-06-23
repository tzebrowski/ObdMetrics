/** 
 * Copyright 2019-2024, Tomasz Żebrowski
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

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.command.ATCommand;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.process.DelayCommand;
import org.obd.metrics.command.process.InitCompletedCommand;
import org.obd.metrics.context.Context;
import org.obd.metrics.pid.PIDsGroup;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class CommandBufferInitHandler {
	
	private static final String PROTOCOL_COMMAND = "SP";

	void prepare(Init init, Adjustments adjustements, Context it) {
		it.register(CommandsBuffer.class, CommandsBuffer.instance()).apply(commandsBuffer -> {
			commandsBuffer.clear();
			init.getSequence().getCommands().stream().forEach(c -> {
				if (c instanceof DelayCommand) {
					log.info("Setting delay after ATZ command: {}", init.getDelayAfterReset());
					((DelayCommand) c).setDelay(init.getDelayAfterReset());
				}
			});
			commandsBuffer.add(init.getSequence());

			// Protocol
			commandsBuffer.addLast(new ATCommand(PROTOCOL_COMMAND + init.getProtocol().getType()));
			appendPIDsGroups(init, adjustements.getRequestedGroups());
			commandsBuffer.addLast(new DelayCommand(init.getDelayAfterInit()));
			commandsBuffer.addLast(new InitCompletedCommand());
		});
	}
	
	private void appendPIDsGroups(Init init, LinkedList<PIDsGroup> groups) {
		log.info("Handling extra groups: {}", groups);

		Context.apply(ctx -> {
			ctx.resolve(PidDefinitionRegistry.class).apply(registry -> {
				groups.forEach(group -> {
					log.info("Adding {} group commands to the queue.", group);
					final List<Command> commands = registry
							.findBy(group).stream()
							.filter(p-> p.getStable())
							.map(p -> mapToCommand(group.getDefaultCommandClass(), p)).filter(Optional::isPresent)
							.map(p -> p.get()).collect(Collectors.toList());
					final CANMessageHeaderManager headerManager = new CANMessageHeaderManager(init);
					headerManager.testSingleMode(commands);
					final CommandsBuffer commandsBuffer = ctx.resolve(CommandsBuffer.class).get();

					commands.forEach(command -> {
						headerManager.switchHeader(command);
						commandsBuffer.addLast(command);
					});
				});
			});
		});
	}

	@SuppressWarnings("unchecked")
	private Optional<Command> mapToCommand(Class<?> defaultClass, PidDefinition pid) {
		try {

			final Class<?> commandClass = (pid.getCommandClass() == null) ? defaultClass
					: Class.forName(pid.getCommandClass());
			if (commandClass == null) {
				return Optional.empty();
			}
			final Constructor<? extends Command> constructor = (Constructor<? extends Command>) commandClass
					.getConstructor(PidDefinition.class);
			return Optional.of(constructor.newInstance(pid));
		} catch (Throwable e) {
			log.error("Failed to initiate command class: {}", pid.getCommandClass(), e);
		}
		return Optional.empty();
	}
}
