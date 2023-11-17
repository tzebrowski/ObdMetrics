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
package org.obd.metrics.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;
import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.api.model.Pids;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.codec.formula.FormulaEvaluatorPolicy;
import org.obd.metrics.connection.MockAdapterConnection;
import org.obd.metrics.pid.Urls;

public class PIDsFileFromStringTest {

	@Test
	public void test() throws IOException, InterruptedException {

		String mode01 = getFileString();
		DataCollector collector = new DataCollector();
		Workflow workflow = Workflow
		        .instance()
		        .formulaEvaluatorPolicy(FormulaEvaluatorPolicy.builder().build())
		        .pids(Pids
		                .builder()
		                .resource(Urls.stringToUrl("mode01", mode01)).build())
		        .observer(collector)
		        .initialize();

		Query query = Query.builder()
		        .pid(6l) // Engine coolant temperature
		        .pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        .pid(16l) // Intake air temperature
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .build();

		final MockAdapterConnection connection = MockAdapterConnection.builder()
		        .requestResponse("0100", "4100be3ea813")
		        .requestResponse("0200", "4140fed00400")
		        .requestResponse("0105", "410522")
		        .requestResponse("010C", "410c541B")
		        .requestResponse("010D", "")
		        .requestResponse("0111", "no data")
		        .requestResponse("010B", "410b35")
		        .readTimeout(0)
		        .build();

		workflow.start(connection, query);

		WorkflowFinalizer.finalize(workflow);

		// Ensure we receive AT command as well
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();

		ObdMetric metric = collector.findSingleMetricBy(workflow.getPidRegistry().findBy(6l));

		Assertions.assertThat(metric.getValue()).isInstanceOf(Integer.class);
		Assertions.assertThat(metric.getValue()).isEqualTo(-6);
		Assertions.assertThat(metric.valueToDouble()).isEqualTo(-6.0);
		Assertions.assertThat(metric.valueToString()).isEqualTo("-6");

	}

	String getFileString() {
		String mode01 = new BufferedReader(
		        new InputStreamReader(Thread
		                .currentThread()
		                .getContextClassLoader()
		                .getResourceAsStream("mode01.json"), StandardCharsets.UTF_8))
		                        .lines()
		                        .collect(Collectors.joining("\n"));
		return mode01;
	}
}
