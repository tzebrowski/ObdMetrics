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

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SupportedPIDsCodec implements Codec<List<String>> {

	@Override
	public List<String> decode(final PidDefinition pid, final ConnectorResponse connectorResponse) {
		if (log.isDebugEnabled()) {
			log.debug("PID[group:{}], processing message: {}", pid.getPid(), connectorResponse.getMessage());
		}

		String rawValue = connectorResponse.getRawValue(pid);
		if (rawValue == null) {
			return Collections.emptyList();
		} else {
			if (rawValue.length() >= 15) {
				rawValue = rawValue.substring(0, 15);
			}

			final long encoded = Long.parseLong(rawValue, 16);

			final String binary = Long.toBinaryString(encoded);
			final List<String> decoded = IntStream.range(1, binary.length()).filter(i -> binary.charAt(i - 1) == '1')
					.mapToObj(i -> String.format("%02x", i)).collect(Collectors.toList());
			if (log.isDebugEnabled()) {
				log.debug("PID[group:{}] supported by ECU: [{}, {} ,{}]", pid.getPid(), encoded, binary, decoded);
			}
			return decoded;
		}
	}
}
