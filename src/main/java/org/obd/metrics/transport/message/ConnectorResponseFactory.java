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
package org.obd.metrics.transport.message;

import org.obd.metrics.pool.ObjectAllocator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class ConnectorResponseFactory {
	
	private static final BytesConnectorResponse EMPTY_CONNECTOR_RESPONSE = new BytesConnectorResponse(0);
	
	private final static ObjectAllocator<BytesConnectorResponse> allocator = 
			ObjectAllocator.of(
					ObjectAllocator.Strategy.Circular,
					BytesConnectorResponse.class, 255);

	public static ConnectorResponse wrap(final byte[] value, int from, int to) {
		final BytesConnectorResponse message = allocator.allocate();
		message.update(value, from, to);
		return message;
	}

	public static ConnectorResponse wrap(final byte[] value) {
		return wrap(value, 0, value.length);
	}
	
	public static ConnectorResponse empty() {
		return EMPTY_CONNECTOR_RESPONSE;
	}
}
