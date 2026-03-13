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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

import org.obd.metrics.pid.PidDefinition;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class SnapshotFormulaEvaluator {

	private final ScriptEngine scriptEngine;
	private final Map<String, CompiledScript> compiledScripts = new ConcurrentHashMap<>();

	SnapshotFormulaEvaluator(final String engineName) {
		scriptEngine = new ScriptEngineManager().getEngineByName(engineName);
	}

	Number evaluate(PidDefinition pid, String rawValueHex) {
		if (!pid.isFormulaAvailable()) {
			return null;
		}

		try {

			final Bindings bindings = createBindings(rawValueHex);
			final CompiledScript compiledScript = getCompiledScript(pid);

			final Object eval = compiledScript.eval(bindings);
			return convertToNumber(pid, eval);

		} catch (Exception e) {
			log.error("Failed to evaluate formula '{}' for PID: {} with raw data '{}'", pid.getFormula(), pid.getPid(),
					rawValueHex, e);
			return null;
		}
	}

	private Bindings createBindings(String rawValueHex) {
		final Bindings bindings = new SimpleBindings();

		final int byteCount = rawValueHex.length() / 2;
		for (int i = 0; i < byteCount; i++) {
			final int byteValue = Integer.parseInt(rawValueHex.substring(i * 2, i * 2 + 2), 16);
			final String varName = String.valueOf((char) (65 + i)); // 65 is 'A'
			bindings.put(varName, byteValue);
		}
		return bindings;
	}

	private CompiledScript getCompiledScript(PidDefinition pid) {
		final CompiledScript compiledScript = compiledScripts.computeIfAbsent(pid.getFormula(), formula -> {
			try {
				return ((Compilable) scriptEngine).compile(formula);
			} catch (Exception e) {
				throw new RuntimeException("Failed to compile script: " + formula, e);
			}
		});
		return compiledScript;
	}

	private Number convertToNumber(PidDefinition pid, Object eval) {
		if (eval == null) {
			return null;
		}

		Number value;
		if (eval instanceof Number) {
			value = (Number) eval;
		} else {
			try {
				value = Double.parseDouble(eval.toString());
			} catch (NumberFormatException e) {
				log.warn("Formula evaluation returned a non-numeric type for PID {}: {}", pid.getPid(), eval);
				return null;
			}
		}

		if (pid.getType() == null) {
			return value.doubleValue();
		}
		
		switch (pid.getType()) {
		case INT:
			return value.intValue();
		case DOUBLE:
			return value.doubleValue();
		case SHORT:
			return value.shortValue();
		default:
			return value;
		}
	}
}
