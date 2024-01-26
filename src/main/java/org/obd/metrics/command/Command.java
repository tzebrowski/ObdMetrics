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

import java.nio.ByteBuffer;
import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(of = { "query" })
public abstract class Command {

	@Getter
	protected final String uid = UUID.randomUUID().toString();

	@Getter
	protected final String query;

	@Getter
	protected final String label;

	@Getter
	protected final byte[] data;

	@Getter
	protected final String mode;

	@Getter
	protected final String canMode;

	protected Command(final String query, final String mode, final String label) {
		this(query, mode, label, "");
	}

	protected Command(final byte[] canId, final byte[] data) {
		this.data = merge(canId, data).array();
		this.query = new String(this.data);
		this.label = null;
		this.mode = null;
		this.canMode = null;
	}

	protected Command(final String query, final String mode, final String label, final String canMode) {
		this.query = query;
		this.label = label;
		this.mode = mode;
		this.data = (query).getBytes();
		this.canMode = canMode;
	}

	@Override
	public String toString() {
		return "[query=" + query + "]";
	}
	
	private ByteBuffer merge(final byte[] canId, final byte[] data) {
		final byte[] allByteArray = new byte[canId.length + 1 + data.length];
		final ByteBuffer buff = ByteBuffer.wrap(allByteArray);
		buff.put(canId);
		buff.put((byte) data.length);
		buff.put(data);
		return buff;
	}
}
