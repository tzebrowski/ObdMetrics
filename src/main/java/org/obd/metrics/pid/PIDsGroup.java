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
package org.obd.metrics.pid;

import org.obd.metrics.command.Command;
import org.obd.metrics.command.SupportedPIDsCommand;
import org.obd.metrics.command.dtc.DiagnosticTroubleCodeClearCommand;
import org.obd.metrics.command.dtc.DiagnosticTroubleCodeCommand;
import org.obd.metrics.command.meta.HexCommand;

import lombok.Getter;

public enum PIDsGroup {

	LIVEDATA(null),
	METADATA(HexCommand.class), 
	DTC_READ(DiagnosticTroubleCodeCommand.class), 
	DTC_CLEAR(DiagnosticTroubleCodeClearCommand.class), 
	CAPABILITES(SupportedPIDsCommand.class);

	@Getter
	private final Class<? extends Command> defaultCommandClass;

	PIDsGroup(Class<? extends Command> defaultCommand) {
		this.defaultCommandClass = defaultCommand;
	}
}
