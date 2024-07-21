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
package org.obd.metrics.codec.formula.backend;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.pid.CommandType;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.Numbers;

import lombok.RequiredArgsConstructor;

final class ScriptEngineParameterBinder {

	@RequiredArgsConstructor
	private final class ParametersBinder implements Numbers {
		private final ScriptEngine scriptEngine;

		@Override
		public void processUnsigned(final int j, final int dec) {
			scriptEngine.put(BINDING_FORMULA_PARAMS.get(j), dec);
		}

		@Override
		public void processSigned(int dec) {
			scriptEngine.put(BINDING_SIGNED_PARAM, dec);
		}

		@Override
		public void processSingle(int dec) {
			scriptEngine.put(BINDING_SINGLE_PARAM, dec);
		}
	}

	private final FormulaEvaluatorConfig formulaEvaluatorConfig;

	private static final List<String> BINDING_FORMULA_PARAMS = IntStream.range(65, 91).boxed()
			.map(ch -> String.valueOf((char) ch.byteValue())).collect(Collectors.toList()); // A - Z

	private static final String BINDING_DEFAULT_PARAM = "A";
	private static final String BINDING_SIGNED_PARAM = "X";
	private static final String BINDING_SINGLE_PARAM = "Y";

	private static final String BINDING_DEBUG_PARAMS = "DEBUG_PARAMS";
	private final ParametersBinder parmsBinder;
	private final Bindings bindings;

	ScriptEngineParameterBinder(final FormulaEvaluatorConfig formulaEvaluatorConfig, final ScriptEngine scriptEngine) {
		this.formulaEvaluatorConfig = formulaEvaluatorConfig;
		this.parmsBinder = new ParametersBinder(scriptEngine);
		this.bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
	}

	void bind(final PidDefinition pidDefinition, final ConnectorResponse connectorResponse) {
		reset();

		bindings.put(BINDING_DEBUG_PARAMS, formulaEvaluatorConfig.getDebug());

		if (isValueNagative(pidDefinition, connectorResponse)) {
			connectorResponse.processNegativeValue(pidDefinition, parmsBinder);
		} else {
			if (CommandType.OBD.equals(pidDefinition.getCommandType())) {
				if (pidDefinition.isFormulaParameterSplitBinding()) {
					connectorResponse.processPositiveValue(pidDefinition, parmsBinder);
				} else {
					connectorResponse.processAsSinglePositiveValue(pidDefinition, parmsBinder);
				}
			} else {
				bindings.put(BINDING_DEFAULT_PARAM, connectorResponse.getMessage());
			}
		}
	}

	private boolean isValueNagative(final PidDefinition pidDefinition, final ConnectorResponse connectorResponse) {
		return pidDefinition.isSigned() && connectorResponse.isValueNegative(pidDefinition);
	}

	private void reset() {
		bindings.remove(BINDING_SIGNED_PARAM);
		BINDING_FORMULA_PARAMS.forEach(p -> {
			bindings.remove(p);
		});
	}
}