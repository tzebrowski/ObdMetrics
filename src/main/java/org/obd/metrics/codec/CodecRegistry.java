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
package org.obd.metrics.codec;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.codec.formula.FormulaEvaluatorCodec;
import org.obd.metrics.codec.formula.FormulaEvaluatorPolicy;
import org.obd.metrics.context.Service;
import org.obd.metrics.pid.PidDefinition;

import lombok.Builder;

public interface CodecRegistry extends Service {

	void register(PidDefinition pid, Codec<?> codec);

	Codec<?> findCodec(PidDefinition pid);

	@Builder
	public static DefaultRegistry of(final FormulaEvaluatorPolicy formulaEvaluatorPolicy,
			final Adjustments adjustments) {

		Codec<Number> evaluator = FormulaEvaluatorCodec.instance(formulaEvaluatorPolicy, adjustments);

		if (adjustments != null && adjustments.getGeneratorPolicy() != null && adjustments.getGeneratorPolicy().isEnabled()) {
			evaluator = new Generator(evaluator, adjustments.getGeneratorPolicy());
		}

		return new DefaultRegistry(evaluator);
	}
}