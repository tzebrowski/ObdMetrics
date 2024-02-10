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
package org.obd.metrics.test.utils;

import java.io.ByteArrayInputStream;
import java.util.concurrent.TimeUnit;

public final class MutableByteArrayInputStream extends ByteArrayInputStream {
	private final long readTimeout;
	private final boolean simulateReadError;

	public MutableByteArrayInputStream(long readTimeout, boolean simulateReadError) {
		super("".getBytes());
		this.readTimeout = readTimeout;
		this.simulateReadError = simulateReadError;
	}

	@Override
	public synchronized int read() {
		if (simulateReadError) {
			throw new RuntimeException("Read exception");
		}

		int read = super.read();
		try {
			TimeUnit.MILLISECONDS.sleep(readTimeout);
		} catch (InterruptedException e) {
		}
		return read;
	}

	public void update(String data) {
		this.buf = data.getBytes();
		this.pos = 0;
		this.count = buf.length;
	}
}