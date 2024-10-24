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
package org.obd.metrics.command;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.obd.metrics.codec.Codec;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SupportedPIDsCommand extends Command implements Codec<List<String>> {

	@Getter
	private final PidDefinition pid;

	public SupportedPIDsCommand(PidDefinition pid) {
		super(pid.getQuery(), pid.getMode(), pid.getDescription());
		this.pid = pid;
	}

	@Override
	public List<String> decode(final PidDefinition pid, final ConnectorResponse connectorResponse) {

		if (log.isDebugEnabled()) {
			log.debug("PID[group:{}], processing message: {}", pid.getPid(), connectorResponse.getMessage());
		}

		if (connectorResponse.isResponseCodeSuccess(pid)) {

			final long encoded = getDecimalAnswerData(pid, connectorResponse);

			final String binary = Long.toBinaryString(encoded);
			final List<String> decoded = IntStream.range(1, binary.length()).filter(i -> binary.charAt(i - 1) == '1')
					.mapToObj(i -> String.format("%02x", i)).collect(Collectors.toList());
			if (log.isDebugEnabled()) {
				log.debug("PID[group:{}] supported by ECU: [{}, {} ,{}]", pid.getPid(), encoded, binary, decoded);
			}
			return decoded;
		} else {
			log.warn("PID[group:{}], failed to transform message: {}", pid.getPid(), connectorResponse.getMessage());
			return Collections.emptyList();
		}
	}

	private Long getDecimalAnswerData(final PidDefinition pidDefinition, final ConnectorResponse connectorResponse) {
		// success code = 0x40 + mode + pid
		String rawAnswerData = getRawAnswerData(pidDefinition, connectorResponse.getMessage());

		if (rawAnswerData.length() > 15) {
			rawAnswerData = rawAnswerData.substring(0, 15);
		}

		return Long.parseLong(rawAnswerData, 16);
	}

	private String getRawAnswerData(final PidDefinition pidDefinition, final String raw) {
		// success code = 0x40 + mode + pid
		return raw.substring(pidDefinition.getSuccessCode().length());
	}
}
