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
package org.obd.metrics.codec.batch.decoder;

import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class IdGenerator {

	private static final int _10 = 10;
	private static final int _100000 = 10000;

	static long generate(final int length, final long pidId, int index,final ConnectorResponse connectorResponse) {
		int postfix = 0;
		long prefix = pidId * _100000;

		if (length >= 1 && connectorResponse.remaining() >= index + 1) {
			int digit = connectorResponse.at(index);
			postfix *= _10;
			postfix += digit;

			digit = connectorResponse.at(++index);
			postfix *= _10;
			postfix += digit;
		}

		if (length >= 2 && connectorResponse.remaining() >= index + 2) {
			int digit = connectorResponse.at(++index);
			postfix *= _10;
			postfix += digit;

			digit = connectorResponse.at(++index);
			postfix *= _10;
			postfix += digit;
			prefix *= 10;
		}

		if (length >= 3 && connectorResponse.remaining() >= index + 2) {
			int digit = connectorResponse.at(++index);
			postfix *= _10;
			postfix += digit;

			digit = connectorResponse.at(++index);
			postfix *= _10;
			postfix += digit;
			prefix *= 100;

		}

		if (length >= 4 && connectorResponse.remaining() >= index + 2) {
			int digit = connectorResponse.at(++index);
			postfix *= _10;
			postfix += digit;

			digit = connectorResponse.at(++index);
			postfix *= _10;
			postfix += digit;
			prefix *= 100;
		}

		final long id = prefix + postfix;
		if (log.isTraceEnabled()) {
			log.trace("{} = {}", pidId, id);
		}
		return id;
	}
}
