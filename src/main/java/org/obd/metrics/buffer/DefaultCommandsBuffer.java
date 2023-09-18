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
package org.obd.metrics.buffer;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingDeque;

import org.obd.metrics.command.Command;
import org.obd.metrics.command.group.CommandGroup;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DefaultCommandsBuffer implements CommandsBuffer {

	// no synchronization need, already synchronized
	private volatile LinkedBlockingDeque<Command> deque = new LinkedBlockingDeque<>();

	@Override
	public CommandsBuffer clear() {
		log.info("Invaldiating {} commands in the queue.", deque.size());
		deque.clear();
		return this;
	}

	@Override
	public long size() {
		return deque.size();
	}

	@Override
	public DefaultCommandsBuffer add(final CommandGroup<?> group) {
		addAll(group.getCommands());
		return this;
	}

	@Override
	public CommandsBuffer addAll(final Collection<? extends Command> commands) {
		commands.forEach(this::addLast);
		return this;
	}

	@Override
	public <T extends Command> CommandsBuffer addFirst(final T command) {
		try {
			deque.putFirst(command);
		} catch (final InterruptedException e) {
			log.warn("Failed to add command to the queue", e);
		}
		return this;
	}

	@Override
	public <T extends Command> CommandsBuffer addLast(final T command) {
		try {
			deque.putLast(command);
		} catch (final InterruptedException e) {
			log.warn("Failed to add command to the queue", e);
		}
		return this;
	}

	@Override
	public Command get() throws InterruptedException {
		return deque.takeFirst();
	}
}
