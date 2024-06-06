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

import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.NumberProcessor;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(of = "mapping")
@EqualsAndHashCode(of = "message")
final class BatchConnectorResponse implements ConnectorResponse {

	private final PIDPositionTemplate mapping;

	private final ConnectorResponse buffer;

	private long id = -1L;

	private boolean cacheable;

	BatchConnectorResponse(final PIDPositionTemplate mapping, final ConnectorResponse buffer) {
		this.mapping = mapping;
		this.buffer = buffer;

		if (mapping == null) {
			this.cacheable = false;
		} else {
			this.cacheable = mapping.getCommand().getPid().getCacheable();
			if (this.cacheable) {
				this.id = IdGenerator.generate(mapping.getCommand().getPid().getLength(),
						mapping.getCommand().getPid().getId(), mapping.getStart(), buffer);
			}
		}
	}

	@Override
	public long capacity() {
		return buffer.capacity();
	}

	@Override
	public String getMessage() {
		return buffer.getMessage();
	}

	@Override
	public int remaining() {
		return buffer.remaining();
	}

	@Override
	public void processUnsignedNumber(final PidDefinition pidDefinition, final NumberProcessor callback) {
		final int messageLength = mapping.getEnd() - mapping.getStart();
		for (int pos = mapping.getStart(), j = 0; pos < mapping.getEnd(); pos += TOKEN_LENGTH, j++) {
			if (messageLength > pidDefinition.getLength() * TOKEN_LENGTH && buffer.byteAt(pos + 1) == COLON) {
				pos += TOKEN_LENGTH;
			}
			callback.processUnsignedNumber(j, getUnsignedNumberBy(pos));
		}
	}

	@Override
	public void processSignedNumber(final PidDefinition pid, final NumberProcessor callback) {
		callback.processSignedNumber(getSignedNumberBy(pid.getLength(), mapping.getStart(), mapping.getEnd()));
	}

	@Override
	public boolean isNegativeNumber(final PidDefinition pid) {
		return (char) byteAt(mapping.getStart()) >= NEGATIVE_CHARACTER;
	}

	@Override
	public boolean isCacheable() {
		return cacheable;
	}

	@Override
	public long id() {
		return id;
	}

	@Override
	public byte byteAt(int index) {
		return buffer.byteAt(index);
	}
}
