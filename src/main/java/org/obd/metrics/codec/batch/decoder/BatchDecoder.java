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
package org.obd.metrics.codec.batch.decoder;

import java.util.List;
import java.util.Map;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.transport.message.ConnectorResponse;

public interface BatchDecoder {

	Map<ObdCommand, ConnectorResponse> decode(final String query, final List<ObdCommand> commands,
			final ConnectorResponse connectorResponse);

	static BatchDecoder get(Adjustments adjustments) {
		if (adjustments == null) {
			adjustments = Adjustments.DEFAULT;
		}

		return new CachedBatchMessageDecoder(adjustments.getBatchPolicy(), adjustments.getCachePolicy());
	}
}
