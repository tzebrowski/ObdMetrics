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
package org.obd.metrics.executor;

import java.util.HashMap;
import java.util.Map;

import org.obd.metrics.buffer.decoder.ConnectorResponseBuffer;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.process.DelayCommand;
import org.obd.metrics.command.process.InitCompletedCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.context.Context;
import org.obd.metrics.transport.Connector;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DefaultCommandHandler implements CommandHandler {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private final Map<Class<? extends Command>, ? extends CommandHandler> registry = new HashMap() {
		private static final long serialVersionUID = 6536620581251911405L;
		{
			put(DelayCommand.class, new DelayCommandHandler());
			put(InitCompletedCommand.class, new InitCompletedHandler());
			put(QuitCommand.class, new QuitCommandHandler());
		}
	};

	private final CommandHandler fallback;

	DefaultCommandHandler() {
		this.fallback = new ObdCommandHandler(Context.instance().resolve(ConnectorResponseBuffer.class).get());
	}

	@Override
	public CommandExecutionStatus execute(Connector connector, Command command) throws Exception {
		log.trace("Executing the command: {}", command);
		return findHandler(command).execute(connector, command);
	}

	private CommandHandler findHandler(Command command) {
		CommandHandler handler = null;
		if (registry.containsKey(command.getClass())) {
			handler = registry.get(command.getClass());
		} else {
			handler = fallback;
		}
		return handler;
	}
}