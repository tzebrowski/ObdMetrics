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
package org.obd.metrics.codec.formula.backend;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.script.Bindings;
import javax.script.SimpleBindings;

import org.obd.metrics.api.model.FormulaExternalParams;
import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.pid.CommandType;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.Numbers;

import lombok.RequiredArgsConstructor;

final class ScriptEngineParameterBinder {

	@RequiredArgsConstructor
	private final class ParametersBinder implements Numbers {
		private final Bindings bindings;

		@Override
		public void processUnsigned(final int j, final int dec) {
			bindings.put(BINDING_FORMULA_PARAMS.get(j), dec);
		}

		@Override
		public void processSigned(int dec) {
			bindings.put(BINDING_SIGNED_PARAM, dec);
		}

		@Override
		public void processSingle(int dec) {
			bindings.put(BINDING_SINGLE_PARAM, dec);
		}
	}

	private final FormulaEvaluatorConfig formulaEvaluatorConfig;
	private final FormulaExternalParams externalParams;

	private static final List<String> BINDING_FORMULA_PARAMS = IntStream.range(65, 91).boxed()
			.map(ch -> String.valueOf((char) ch.byteValue())).collect(Collectors.toList());

	private static final String BINDING_DEFAULT_PARAM = "A";
	private static final String BINDING_SIGNED_PARAM = "X";
	private static final String BINDING_SINGLE_PARAM = "Y";
	private static final String BINDING_DEBUG_PARAMS = "DEBUG_PARAMS";

	ScriptEngineParameterBinder(final FormulaEvaluatorConfig formulaEvaluatorConfig,
			final FormulaExternalParams externalParams) {
		this.formulaEvaluatorConfig = formulaEvaluatorConfig;
		this.externalParams = externalParams;
	}

	// Now returns a fresh, thread-safe Bindings context for evaluation
	Bindings bind(final PidDefinition pidDefinition, final ConnectorResponse connectorResponse) {
		final Bindings bindings = new SimpleBindings();
		final ParametersBinder parmsBinder = new ParametersBinder(bindings);

		bindings.put(BINDING_DEBUG_PARAMS, formulaEvaluatorConfig.getDebug());
		bindings.putAll(externalParams.getParams());

		if (isValueNegative(pidDefinition, connectorResponse)) {
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
		
		return bindings;
	}

	private boolean isValueNegative(final PidDefinition pidDefinition, final ConnectorResponse connectorResponse) {
		return pidDefinition.isSigned() && connectorResponse.isValueNegative(pidDefinition);
	}
}