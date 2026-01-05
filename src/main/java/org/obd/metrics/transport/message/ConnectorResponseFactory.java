 /**
 * Copyright 2019-2026, Tomasz Żebrowski
 *
 * <p>Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.obd.metrics.transport.message;

import org.obd.metrics.pool.ObjectAllocator;
import org.obd.metrics.transport.BufferSize;

public final class ConnectorResponseFactory {

	public static final RawConnectorResponse EMPTY_CONNECTOR_RESPONSE = new RawConnectorResponse(0);
	private final ObjectAllocator<RawConnectorResponse> allocator;

	public ConnectorResponseFactory() {
		this(255, BufferSize.DEFAULT);
	}

	public ConnectorResponseFactory(int bufferSize) {
		this(255, bufferSize);
	}
	
	public ConnectorResponseFactory(int allocatorSize, int bufferSize) {
		this.allocator = ObjectAllocator.of(ObjectAllocator.Strategy.Circular, RawConnectorResponse.class, allocatorSize,
				bufferSize);
	}

	public ConnectorResponse wrap(final byte[] value, int from, int to) {
		final RawConnectorResponse raw = allocator.allocate();
		raw.update(value, from, to);
		return raw;
	}

	public static ConnectorResponse wrap(final byte[] value) {
		return new ConnectorResponseFactory(1, BufferSize.DEFAULT).wrap(value, 0, value.length);
	}
}
