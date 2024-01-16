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
package org.obd.metrics.codec.formula;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.codec.formula.backend.FormulaEvaluatorBackend;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class FormulaEvaluator implements FormulaEvaluatorCodec {

	private final FormulaEvaluatorBackend backed;
	private final FormulaEvaluatorCache cache;

	FormulaEvaluator(FormulaEvaluatorPolicy formulaEvaluatorPolicy, final Adjustments adjustments) {
		if (formulaEvaluatorPolicy == null) {
			formulaEvaluatorPolicy = FormulaEvaluatorPolicy.builder().build();
		}
		this.backed = FormulaEvaluatorBackend.script(formulaEvaluatorPolicy);
		this.cache = new FormulaEvaluatorCache(
				adjustments == null ? CachePolicy.DEFAULT : adjustments.getCachePolicy());
	}

	@Override
	public Number decode(final PidDefinition pid, final ConnectorResponse connectorResponse) {
		if (log.isDebugEnabled()) {
			log.debug("Found PID definition: {}", pid);
		}
		if (connectorResponse.isResponseCodeSuccess(pid)) {
			if (pid.isFormulaAvailable()) {
				if (cache.contains(connectorResponse)) {
					return cache.get(connectorResponse);
				} else {
					final Number result = backed.evaluate(pid, connectorResponse);
					cache.put(connectorResponse, result);
					return result;
				}
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
