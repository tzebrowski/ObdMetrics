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
package org.obd.metrics.command.dtc;

import org.obd.metrics.codec.Codec;
import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class DiagnosticTroubleCodeSnapshotCodec implements Codec<Void, UdsSnapshotResponse> {

	private UdsSnapshotParser parser;

	public DiagnosticTroubleCodeSnapshotCodec(final FormulaEvaluatorConfig formulaEvaluationConfig,
			final PidDefinitionRegistry registry) {

		try {
			this.parser = new UdsSnapshotParser(registry, formulaEvaluationConfig.getScriptEngine());
		} catch (Throwable e) {
			this.parser = null;
			log.error("Failed to initialize parser", e);
		}
	}

	@Override
	public UdsSnapshotResponse decode(final PidDefinition pid, final ConnectorResponse connectorResponse) {

		if (connectorResponse.isEmpty() || parser == null) {
			// An answer all the same (NO DATA, no parser): reported so the handler stops waiting for it.
			final UdsSnapshotResponse empty = new UdsSnapshotResponse();
			empty.setErrorMessage(parser == null ? "No snapshot parser" : "Empty snapshot response");
			return empty;
		} else {
			final UdsSnapshotResponse udsResponse = parser.parse(connectorResponse.getMessage());

			if (udsResponse.hasError()) {
				log.error("Error: {} (DTC {}, {} DIDs decoded)", udsResponse.getErrorMessage(), udsResponse.getDtcHex(),
						udsResponse.getExtractedDids().size());
			} else if (log.isDebugEnabled()) {
				log.debug("Reassembled Payload: {}", udsResponse.getRawDataBlock());
			}
			// Returned even on error: the DIDs decoded before the parser stopped are still shown, and the reply must
			// reach DiagnosticTroubleCodeHandler - a dropped (null) reply left it waiting for the full timeout.
			return udsResponse;
		}
	}
}
