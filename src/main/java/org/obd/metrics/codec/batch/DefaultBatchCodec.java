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
package org.obd.metrics.codec.batch;

import java.util.List;
import java.util.Map;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.codec.Encoder;
import org.obd.metrics.codec.batch.decoder.BatchDecoder;
import org.obd.metrics.codec.batch.encoder.BatchEncoder;
import org.obd.metrics.command.obd.BatchObdCommand;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DefaultBatchCodec implements BatchCodec {

	protected static final int DEFAULT_BATCH_SIZE = 6;

	protected final List<ObdCommand> commands;
	protected final String query;

	protected final BatchDecoder decoder;
	protected final Encoder<BatchObdCommand> encoder;

	DefaultBatchCodec(final BatchCodecType codecType, final Init init, final Adjustments adjustments,
			final String query, final List<ObdCommand> commands) {
		this.query = query;
		this.commands = commands;
		this.decoder = BatchDecoder.get(adjustments);
		this.encoder = BatchEncoder.get(this, codecType, init, adjustments, query, commands);
	}

	@Override
	public Map<ObdCommand, ConnectorResponse> decode(final PidDefinition pid,
			final ConnectorResponse connectorResponse) {
		return decoder.decode(query, commands, connectorResponse);
	}

	@Override
	public Map<ObdCommand, ConnectorResponse> decode(final String query, final List<ObdCommand> commands,
			final ConnectorResponse connectorResponse) {
		return decoder.decode(query, commands, connectorResponse);
	}

	@Override
	public List<BatchObdCommand> encode() {
		return encoder.encode();
	}
}
