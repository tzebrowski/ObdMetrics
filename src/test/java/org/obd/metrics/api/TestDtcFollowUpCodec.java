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
package org.obd.metrics.api;

import org.obd.metrics.api.model.SnapshotPID;
import org.obd.metrics.codec.Codec;
import org.obd.metrics.command.dtc.DtcFollowUpResponse;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;

// Test codec for a dtcFollowUp template: "5906 <dtc 3B> <status> <record> <value...>", the last byte is the value.
public final class TestDtcFollowUpCodec implements Codec<Void, DtcFollowUpResponse> {

	@Override
	public DtcFollowUpResponse decode(final PidDefinition pid, final ConnectorResponse connectorResponse) {
		final String message = connectorResponse.getMessage();
		if (message == null || !message.startsWith("5906") || message.length() < 16) {
			return null;
		}
		final DtcFollowUpResponse response = new DtcFollowUpResponse();
		response.setDtcHex(message.substring(4, 10));
		final String valueHex = message.substring(message.length() - 2);
		response.addValue(new SnapshotPID(pid, valueHex, Integer.parseInt(valueHex, 16)));
		return response;
	}
}
