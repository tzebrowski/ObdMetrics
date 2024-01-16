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
package org.obd.metrics.buffer;

import java.util.Collection;

import org.obd.metrics.command.Command;
import org.obd.metrics.command.group.CommandGroup;
import org.obd.metrics.context.Service;

public interface CommandsBuffer extends Service {
	CommandsBuffer clear();

	long size();

	CommandsBuffer add(CommandGroup<?> group);

	CommandsBuffer addAll(Collection<? extends Command> commands);

	<T extends Command> CommandsBuffer addFirst(T command);

	<T extends Command> CommandsBuffer addLast(T command);

	Command get() throws InterruptedException;

	static CommandsBuffer instance() {
		return new DefaultCommandsBuffer();
	}
}