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
package org.obd.metrics.api.model;

import org.obd.metrics.command.Command;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;


@SuperBuilder
@EqualsAndHashCode(of = "command")
public class Reply<T extends Command> {

	
	@Getter
	protected final T command;

	
	@Getter
	protected final ConnectorResponse raw;

	@Getter
	protected final long timestamp = System.currentTimeMillis();
	
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(100);
		builder.append("Reply [com=");
		builder.append(command);
		builder.append(", raw=");
		builder.append(raw);
		builder.append("]");
		return builder.toString();
	}
}
