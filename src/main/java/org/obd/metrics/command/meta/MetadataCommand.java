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
package org.obd.metrics.command.meta;

import java.util.Optional;

import org.obd.metrics.command.Command;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.Characters;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class MetadataCommand extends Command {
	private static final String pattern = "[a-zA-Z0-9]{1}\\:";

	protected final PidDefinition pid;

	protected MetadataCommand(PidDefinition pid) {
		super(pid.getQuery(), pid.getService(), pid.getDescription());
		this.pid = pid;
	}

	protected Optional<String> decodeRawMessage(final String command, final ConnectorResponse connectorResponse) {
		final String message = command.replaceAll(" ", "");
		final int leadingSuccessCodeNumber = message.charAt(0) + 4;
		final String successCode = (char) (leadingSuccessCodeNumber) + message.substring(1);
		
		final String normazlizedAnswer = Characters.normalize(connectorResponse.getMessage());
		final int indexOfSuccessCode = normazlizedAnswer.indexOf(successCode);

		if (indexOfSuccessCode >= 0) {
			final String normalizedMsg = normazlizedAnswer.substring(indexOfSuccessCode + successCode.length()).replaceAll(pattern,
					"");

			if (log.isTraceEnabled()) {
				log.trace("successCode= '{}', indexOfSuccessCode='{}',normalizedMsg='{}'", successCode,
						indexOfSuccessCode, normalizedMsg);
			}
			return Optional.of(normalizedMsg);
		} else {
			log.warn("Failed to decode message. Invalid answer code. Message:{}", message);
			return Optional.empty();
		}
	}
}
