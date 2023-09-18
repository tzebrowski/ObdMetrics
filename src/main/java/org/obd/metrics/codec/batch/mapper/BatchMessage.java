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
package org.obd.metrics.codec.batch.mapper;

import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.DecimalReceiver;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(of = "mapping")
@EqualsAndHashCode(of = "message")
final class BatchMessage implements ConnectorResponse {

	private static final byte COLON = 58;

	private final BatchCommandMapping mapping;

	private final ConnectorResponse delegate;

	private long id = -1L;

	private boolean cacheable;

	BatchMessage(final BatchCommandMapping mapping, final ConnectorResponse delegate) {
		this.mapping = mapping;
		this.delegate = delegate;

		if (mapping == null) {
			this.cacheable = false;
		} else {
			this.cacheable = mapping.getCommand().getPid().getCacheable();
			if (this.cacheable) {
				this.id = IdGenerator.generate(mapping.getCommand().getPid().getLength(),
						mapping.getCommand().getPid().getId(), mapping.getStart(), delegate);
			}
		}
	}

	@Override
	public long capacity() {
		return delegate.capacity();
	}

	@Override
	public String getMessage() {
		return delegate.getMessage();
	}

	@Override
	public int remaining() {
		return delegate.remaining();
	}

	@Override
	public void exctractDecimals(final PidDefinition pidDefinition, final DecimalReceiver decimalReceiver) {
		final int messageLength = mapping.getEnd() - mapping.getStart();
		for (int pos = mapping.getStart(), j = 0; pos < mapping.getEnd(); pos += 2, j++) {
			if (messageLength > pidDefinition.getLength() * 2 && delegate.byteAt(pos + 1) == COLON) {
				pos += 2;
			}
			
			decimalReceiver.receive(j, toDecimal(pos));
		}
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
		return delegate.byteAt(index);
	}
}
