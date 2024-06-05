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
import org.obd.metrics.transport.message.DecimalReceiver;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class ScriptEngineParameterInjector implements DecimalReceiver {

	private final FormulaEvaluatorConfig formulaEvaluatorConfig;

	private static final List<String> FORMULA_PARAMS = IntStream.range(65, 91).boxed()
			.map(ch -> String.valueOf((char) ch.byteValue())).collect(Collectors.toList()); // A - Z

	private final ScriptEngine scriptEngine;
	private static final String BINDING_SIGNED_CHAR = "X";
	private static final String BINDING_DEBUG_PARAMS = "DEBUG_PARAMS";

	@Override
	public void receive(final int j, final int dec) {
		scriptEngine.put(FORMULA_PARAMS.get(j), dec);
	}

	@Override
	public void receive(double dec) {
		scriptEngine.put(BINDING_SIGNED_CHAR, dec);
	}

	void inject(final PidDefinition pidDefinition, final ConnectorResponse connectorResponse) {
		reset();

		if (pidDefinition.isSigned() && connectorResponse.isNegative(pidDefinition)) {
			connectorResponse.exctractSingleDecimal(pidDefinition, this);
		} else {
			injectFormulaParameters(pidDefinition, connectorResponse);
		}
	}

	private void reset() {
		final Bindings bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);

		bindings.remove(BINDING_SIGNED_CHAR);
		FORMULA_PARAMS.forEach(p -> {
			bindings.remove(p);
		});
	}

	private void injectFormulaParameters(final PidDefinition pidDefinition, final ConnectorResponse connectorResponse) {

		scriptEngine.put(BINDING_DEBUG_PARAMS, formulaEvaluatorConfig.getDebug());

		if (CommandType.OBD.equals(pidDefinition.getCommandType())) {
			connectorResponse.exctractDecimals(pidDefinition, this);
		} else {
			scriptEngine.put("A", connectorResponse.getMessage());
		}
	}
}