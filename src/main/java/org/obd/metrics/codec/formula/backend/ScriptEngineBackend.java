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
package org.obd.metrics.codec.formula.backend;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class ScriptEngineBackend implements FormulaEvaluatorBackend {

	private final ScriptEngine scriptEngine;

	private final ScriptEngineParameterInjector engineParameterInjector;

	ScriptEngineBackend(FormulaEvaluatorConfig formulaEvaluatorConfig) {
		log.info("Creating formula evaluator for {}", formulaEvaluatorConfig);
		this.scriptEngine = new ScriptEngineManager().getEngineByName(formulaEvaluatorConfig.getScriptEngine());
		this.engineParameterInjector = new ScriptEngineParameterInjector(formulaEvaluatorConfig, scriptEngine);
	}

	@Override
	public Number evaluate(final PidDefinition pid, final ConnectorResponse connectorResponse) {

		try {
			engineParameterInjector.injectFormulaParameters(pid, connectorResponse);
			final Object eval = scriptEngine.eval(pid.getFormula());
			return TypesConverter.convert(pid, eval);
		} catch (final Throwable e) {
			if (log.isTraceEnabled()) {
				log.trace("Failed to evaluate the formula {} for PID: {}, message: {}", pid.getFormula(), pid.getPid(),
						connectorResponse.getMessage(), e);
			}

			log.error("Failed to evaluate the formula {} for PID: {}", pid.getFormula(), pid.getPid());
		}
		return null;
	}
}
