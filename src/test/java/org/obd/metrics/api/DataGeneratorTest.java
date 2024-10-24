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
package org.obd.metrics.api;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.codec.GeneratorPolicy;
import org.obd.metrics.diagnostic.Histogram;
import org.obd.metrics.diagnostic.RateType;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.pid.ValueType;
import org.obd.metrics.test.DataCollector;
import org.obd.metrics.test.MockAdapterConnection;
import org.obd.metrics.test.SimpleWorkflowFactory;
import org.obd.metrics.test.WorkflowFinalizer;

public class DataGeneratorTest {

	@Test
	public void generatorTest() throws IOException, InterruptedException {
		Workflow workflow = SimpleWorkflowFactory.getWorkflow();

		Query query = Query.builder()
		        .pid(6008l) // Coolant
		        .pid(6004l) // RPM
		        .pid(6007l) // Intake temp
		        .pid(6015l)// Oil temp
		        .pid(603l) // Spark Advance
		        .build();

		MockAdapterConnection connection = MockAdapterConnection.builder()
		        .requestResponse("221003", "62100340")
		        .requestResponse("221000", "xxxxxxxxxxxxxx")
		        .requestResponse("221935", "xxxxxxxxxxxxxx")
		        .requestResponse("22194f", "xxxxxxxxxxxxxx")
		        .requestResponse("221812", "")
		        .build();

		Adjustments optional = Adjustments
		        .builder()
		        .generatorPolicy(GeneratorPolicy.builder().increment(5.0).enabled(true).build())
		        .build();

		workflow.start(connection, query, optional);

		WorkflowFinalizer.finalizeAfter(workflow,800);

		PidDefinitionRegistry pids = workflow.getPidRegistry();

		PidDefinition pid8l = pids.findBy(6008l);

		Assertions.assertThat(workflow.getDiagnostics().rate().findBy(RateType.MEAN,pid8l).get().getValue()).isGreaterThan(0);

		Histogram histogram = workflow.getDiagnostics().histogram().findBy(pid8l);
		Assertions.assertThat(histogram.getMax()).isGreaterThan(histogram.getMin());
		Assertions.assertThat(histogram.getMin()).isLessThan(histogram.getMean());
		Assertions.assertThat(histogram.getMean()).isLessThan(histogram.getMax()).isGreaterThan(histogram.getMin());
	}

	@Test
	public void defaultIncrementTest() throws IOException, InterruptedException {

		Workflow workflow = SimpleWorkflowFactory.getWorkflow();

		Query query = Query.builder()
		        .pid(6008l) // Coolant
		        .pid(6004l) // RPM
		        .pid(6007l) // Intake temp
		        .pid(60015l)// Oil temp
		        .pid(6003l) // Spark Advance
		        .build();

		MockAdapterConnection connection = MockAdapterConnection.builder()
		        .requestResponse("221003", "62100340")
		        .requestResponse("221000", "xxxxxxxxxxxxxx")
		        .requestResponse("221935", "xxxxxxxxxxxxxx")
		        .requestResponse("22194f", "xxxxxxxxxxxxxx")
		        .requestResponse("221812", "")
		        .build();

		Adjustments optional = Adjustments
		        .builder()
		        .generatorPolicy(GeneratorPolicy.builder().enabled(true).build())
		        .build();

		workflow.start(connection, query, optional);

		WorkflowFinalizer.finalizeAfter(workflow,800);

		PidDefinitionRegistry pids = workflow.getPidRegistry();

		PidDefinition pid8l = pids.findBy(6008l);

		Assertions.assertThat(workflow.getDiagnostics().rate().findBy(RateType.MEAN,pid8l).get().getValue()).isGreaterThan(0);

		Histogram histogram = workflow.getDiagnostics().histogram().findBy(pid8l);

		Assertions.assertThat(histogram.getMax()).isGreaterThan(histogram.getMin());
		Assertions.assertThat(histogram.getMin()).isLessThan(histogram.getMean());
		Assertions.assertThat(histogram.getMean()).isLessThan(histogram.getMax()).isGreaterThan(histogram.getMin());
	}

	@Test
	public void smartTest() throws IOException, InterruptedException {

		DataCollector collector = new DataCollector();
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(collector);

		PidDefinitionRegistry pidRegistry = workflow.getPidRegistry();
		pidRegistry.register(new PidDefinition(10001l, 2, "((A *256 ) +B)/4", "22", "2000", "rpm", "Engine RPM",
		        0, 8000, ValueType.DOUBLE));
		pidRegistry.register(new PidDefinition(10002l, 2, "((A *256 ) +B)/4", "22", "2002", "rpm", "Engine RPM",
		        2, 8000, ValueType.DOUBLE));
		pidRegistry.register(new PidDefinition(10003l, 2, "((A *256 ) +B)/4", "22", "2004", "rpm", "Engine RPM",
		        5, 8000, ValueType.DOUBLE));

		pidRegistry.register(new PidDefinition(10004l, 2, "((A *256 ) +B)/4", "22", "2006", "rpm", "Engine RPM",
		        20, 100, ValueType.DOUBLE));

		pidRegistry.register(new PidDefinition(10005l, 2, "((A *256 ) +B)/4", "22", "2008", "rpm", "Engine RPM",
		        0, 7000, ValueType.DOUBLE));

		Query query = Query.builder()
		        .pid(10001l) // Coolant
		        .pid(10002l) // RPM
		        .pid(10003l) // Intake temp
		        .pid(10004l) // Oil temp
		        .pid(10005l) // Spark Advance
		        .build();

		MockAdapterConnection connection = MockAdapterConnection.builder()
		        .requestResponse("222000", "6220000BEA")
		        .requestResponse("222002", "6220020BEA")
		        .requestResponse("222004", "6220040BEA")
		        .requestResponse("222006", "6220060BEA")
		        .requestResponse("222008", "6220080BEA")
		        .build();

		Adjustments optional = Adjustments.builder()
		        .generatorPolicy(GeneratorPolicy.builder().enabled(true).build())
		        .build();

		workflow.start(connection, query, optional);

		WorkflowFinalizer.finalizeAfter(workflow,800);

		ObdMetric metric = collector.findSingleMetricBy(workflow.getPidRegistry().findBy(10002l));
		Assertions.assertThat(metric).isNotNull();
		Assertions.assertThat(metric.getValue()).isNotNull().isInstanceOf(Double.class);

		metric = collector.findSingleMetricBy(workflow.getPidRegistry().findBy(10001l));
		Assertions.assertThat(metric).isNotNull();
		Assertions.assertThat(metric.getValue()).isNotNull().isInstanceOf(Double.class);

		metric = collector.findSingleMetricBy(workflow.getPidRegistry().findBy(10003l));
		Assertions.assertThat(metric).isNotNull();
		Assertions.assertThat(metric.getValue()).isNotNull().isInstanceOf(Double.class);
	}
}
