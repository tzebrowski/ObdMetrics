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
package org.obd.metrics.buffer.decoder;

import java.util.concurrent.LinkedBlockingDeque;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DefaultConnectorResponseBuffer implements ConnectorResponseBuffer {
	private volatile LinkedBlockingDeque<ConnectorResponseWrapper> deque = new LinkedBlockingDeque<>();

	@Override
	public ConnectorResponseBuffer clear() {
		log.info("Invaldiating {} commands in the queue.", deque.size());
		deque.clear();
		return this;
	}

	@Override
	public ConnectorResponseBuffer addLast(ConnectorResponseWrapper command) {
		try {
			deque.putLast(command);
		} catch (final InterruptedException e) {
			log.warn("Failed to add command to the queue", e);
		}
		return this;
	}

	@Override
	public ConnectorResponseWrapper get() throws InterruptedException {
		return deque.takeFirst();
	}
}
