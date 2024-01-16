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
package org.obd.metrics.command.dtc;

import org.obd.metrics.codec.Codec;
import org.obd.metrics.command.Command;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class DiagnosticTroubleCodeClearCommand extends Command
		implements Codec<DiagnosticTroubleCodeClearStatus> {

	protected final PidDefinition pid;

	public DiagnosticTroubleCodeClearCommand(PidDefinition pid) {
		super(pid.getQuery(), pid.getMode(), pid.getDescription());
		this.pid = pid;
	}

	@Override
	public DiagnosticTroubleCodeClearStatus decode(final PidDefinition pidDef,
			final ConnectorResponse connectorResponse) {

		final String message = connectorResponse.getMessage();

		log.info("Received following response for DTC Clear operation: {}", connectorResponse.getMessage());
		if (message.startsWith(pid.getSuccessCode())) {
			log.debug("Operation of DTC cleaning completed successfully");
			return DiagnosticTroubleCodeClearStatus.OK;
		} else {
			log.debug("Operation of DTC cleaning failed.");
			return DiagnosticTroubleCodeClearStatus.ERR;
		}
	}
}
