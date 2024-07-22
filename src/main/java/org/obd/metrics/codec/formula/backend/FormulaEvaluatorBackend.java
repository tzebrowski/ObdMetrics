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

import org.obd.metrics.api.model.FormulaExternalParams;
import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;

public interface FormulaEvaluatorBackend {

	Number evaluate(PidDefinition pid, ConnectorResponse connectorResponse);

	public static FormulaEvaluatorBackend of(final FormulaEvaluatorConfig formulaEvaluatorConfig,
			final FormulaExternalParams unitsConversionPolicy) {
		return new ScriptEngineBackend(formulaEvaluatorConfig, unitsConversionPolicy);
	}
}