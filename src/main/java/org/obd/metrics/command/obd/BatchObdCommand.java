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
package org.obd.metrics.command.obd;

import java.util.List;
import java.util.Objects;

import org.obd.metrics.codec.batch.BatchCodec;

import lombok.Getter;

public class BatchObdCommand extends ObdCommand {

	@Getter
	private final int priority;

	@Getter
	private final BatchCodec codec;
	private final String mode;
	private final String canMode;
	
	public BatchObdCommand(final BatchCodec codec, final String query, final List<ObdCommand> commands,
			final int priority) {
		super(query);
		this.priority = priority;
		this.codec = codec;
		this.mode = commands.get(0).getMode();
		this.canMode = commands.get(0).getCanMode();
	}

	@Override
	public String getCanMode() {
		return canMode;
	}
	
	@Override
	public String getMode() {
		return mode;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(query);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		BatchObdCommand other = (BatchObdCommand) obj;
		return Objects.equals(query, other.query);
	}
}
