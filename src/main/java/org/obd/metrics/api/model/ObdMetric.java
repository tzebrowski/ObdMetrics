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

import org.obd.metrics.command.obd.ObdCommand;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class ObdMetric extends Reply<ObdCommand> {

	private static final String NO_DATA_MESSAGE = "No data";
	private static final int multiplier = (int) Math.pow(10, 2);

	@Getter
	private final boolean alert;
	
	@Getter
	private final Number value;

	public double valueToDouble() {
		return value == null ? Double.NaN
		        : (double) ((long) (value.doubleValue() * multiplier)) / multiplier;
	}

	public String valueToString() {
		if (getValue() == null) {
			return NO_DATA_MESSAGE;
		} else {
			if (getValue() instanceof Double) {
				return String.valueOf(valueToDouble());
			} else {
				return getValue().toString();
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ObdMetric [pid=");
		builder.append(command.getPid().getPid());
		builder.append(", id=");
		builder.append(command.getPid().getId());
		builder.append(", val=");
		builder.append(value);
		builder.append(", raw=");
		builder.append(raw);
		builder.append("]");
		return builder.toString();
	}	
}
