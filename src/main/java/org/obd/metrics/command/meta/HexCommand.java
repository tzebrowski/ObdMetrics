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

import org.obd.metrics.codec.Codec;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class HexCommand extends MetadataCommand implements Codec<String> {

	public HexCommand(PidDefinition pid) {
		super(pid);
	}

	@Override
	public String decode(PidDefinition pid, ConnectorResponse connectorResponse) {

		log.info("Decoding the message: {}", connectorResponse.getMessage());
		final Optional<String> answer = decodeRawMessage(getQuery(),connectorResponse);

		if (answer.isPresent()) {
			final String decoded = Hex.decode(answer.get());
			final String result = (decoded == null) ? null : decoded.trim();
			log.info("Decoded message: {} for: {}", result, connectorResponse.getMessage());
			return result;
		}
		return null;
	}
}
