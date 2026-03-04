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
package org.obd.metrics.codec.formula;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.FormulaExternalParams;
import org.obd.metrics.codec.formula.backend.FormulaEvaluatorBackend;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class FormulaEvaluator implements FormulaEvaluatorCodec {

	private final FormulaEvaluatorBackend backend;
	private final FormulaEvaluatorCache cache;
	private static final FormulaEvaluatorConfig DEFAULT = FormulaEvaluatorConfig.builder().build();

	FormulaEvaluator(FormulaEvaluatorConfig formulaEvaluatorConfig, final Adjustments adjustments) {
		
		if (formulaEvaluatorConfig == null) {
			formulaEvaluatorConfig = DEFAULT;
		}
		
		this.backend = FormulaEvaluatorBackend.of(formulaEvaluatorConfig,
				adjustments == null ? FormulaExternalParams.DEFAULT : adjustments.getFormulaExternalParams());
		this.cache = new FormulaEvaluatorCache(
				adjustments == null ? CachePolicy.DEFAULT : adjustments.getCachePolicy());
	}

	@Override
	public Number decode(final PidDefinition pid, final ConnectorResponse connectorResponse) {
		if (connectorResponse.isResponseCodeSuccess(pid)) {
			if (pid.isFormulaAvailable()) {
				
				// Delegate entirely to the atomic computeIfAbsent method
				return cache.computeIfAbsent(connectorResponse, () -> backend.evaluate(pid, connectorResponse));

			} else {
				if (log.isDebugEnabled()) {
					log.debug("No formula found in {} for: {}", pid, connectorResponse);
				}
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Answer code is incorrect for: {}", connectorResponse.getMessage());
			}
		}
		return null;
	}
}