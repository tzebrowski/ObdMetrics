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
import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.ValueType;
import org.obd.metrics.test.utils.DataCollector;
import org.obd.metrics.test.utils.MockAdapterConnection;
import org.obd.metrics.test.utils.SimpleWorkflowFactory;
import org.obd.metrics.test.utils.WorkflowFinalizer;

public class DataConversionTest {

	@Test
	public void typesConversionTest() throws IOException, InterruptedException {

		DataCollector collector = new DataCollector();
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(collector);

		workflow.getPidRegistry()
		        .register(new PidDefinition(10001l, 2, "A + B", "22", "2000", "rpm", "Engine RPM",
		                0, 8000, ValueType.INT));
		workflow.getPidRegistry()
		        .register(new PidDefinition(10002l, 2, "A + B", "22", "2002", "rpm", "Engine RPM",
		                0, 8000, ValueType.SHORT));
		workflow.getPidRegistry()
		        .register(new PidDefinition(10003l, 2, "A + B", "22", "2004", "rpm", "Engine RPM",
		                0, 8000, ValueType.DOUBLE));

		Query query = Query.builder()
		        .pid(10001l)
		        .pid(10002l)
		        .pid(10003l)
		        .build();

		MockAdapterConnection connection = MockAdapterConnection
		        .builder()
		        .requestResponse("222000", "6220000BEA")
		        .requestResponse("222002", "6220020BEA")
		        .requestResponse("222004", "6220040BEA")
		        .build();

		workflow.start(connection, query);

		WorkflowFinalizer.finalize(workflow);

		ObdMetric metric = collector.findSingleMetricBy(workflow.getPidRegistry().findBy(10002l));
		Assertions.assertThat(metric).isNotNull();
		Assertions.assertThat(metric.getValue()).isNotNull().isInstanceOf(Short.class);

		metric = collector.findSingleMetricBy(workflow.getPidRegistry().findBy(10001l));
		Assertions.assertThat(metric).isNotNull();

		Assertions.assertThat(metric.getValue()).isNotNull().isInstanceOf(Integer.class);

		metric = collector.findSingleMetricBy(workflow.getPidRegistry().findBy(10003l));
		Assertions.assertThat(metric).isNotNull();

		Assertions.assertThat(metric.getValue()).isNotNull().isInstanceOf(Double.class);
	}

	@Test
	public void invalidFormulaTest() throws IOException, InterruptedException {
		DataCollector collector = new DataCollector();
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(collector);

		long id = 10001l;

		String invalidFormula = "(A *256 ) +B )/4";
		workflow.getPidRegistry()
		        .register(new PidDefinition(id, 2, invalidFormula, "22", "2000", "rpm", "Engine RPM", 0, 100,
		                ValueType.DOUBLE));

		Query query = Query.builder()
		        .pid(id)
		        .build();

		MockAdapterConnection connection = MockAdapterConnection.builder()
		        .requestResponse("222000", "6220000BEA")
		        .build();

		workflow.start(connection, query);

		WorkflowFinalizer.finalize(workflow);

		ObdMetric metric = collector.findSingleMetricBy(workflow.getPidRegistry().findBy(id));
		Assertions.assertThat(metric).isNull();
	}

	@Test
	public void noFormulaTest() throws IOException, InterruptedException {
		DataCollector collector = new DataCollector();
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(collector);

		long id = 10001l;
		workflow.getPidRegistry().register(
		        new PidDefinition(id, 2, "", "22", "2000", "rpm", "Engine RPM", 0, 8000, ValueType.DOUBLE));

		Query query = Query.builder()
		        .pid(id)
		        .build();

		final MockAdapterConnection connection = MockAdapterConnection.builder()
		        .requestResponse("222000", "6220000BEA")
		        .build();

		workflow.start(connection, query);

		WorkflowFinalizer.finalize(workflow);

		ObdMetric metric = collector.findSingleMetricBy(workflow.getPidRegistry().findBy(id));
		Assertions.assertThat(metric).isNull();
	}

	@Test
	public void invalidaDataTest() throws IOException, InterruptedException {
		DataCollector collector = new DataCollector();
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(collector);

		long id = 10001l;
		workflow.getPidRegistry()
		        .register(new PidDefinition(id, 2, "(A *256 ) +B )/4", "22", "2000", "rpm", "Engine RPM", 0,
		        		8000, ValueType.DOUBLE));

		// Query for specified PID's like RPM
		Query query = Query.builder()
		        .pid(id) // Coolant
		        .pid(6008l) // Coolant
		        .pid(6004l) // RPM
		        .pid(6007l) // Intake temp
		        .pid(6015l)// Oil temp
		        .pid(603l) // Spark Advance
		        .build();

		MockAdapterConnection connection = MockAdapterConnection.builder()
		        .requestResponse("222000", "xxxxxxxxxxxxxx")
		        .requestResponse("221000", "")
		        .requestResponse("221935", "nodata")
		        .requestResponse("22194f", "stopped")
		        .requestResponse("221812", "unabletoconnect")
		        .build();

		workflow.start(connection, query);

		WorkflowFinalizer.finalize(workflow);

		ObdMetric metric = collector.findSingleMetricBy(workflow.getPidRegistry().findBy(id));
		Assertions.assertThat(metric).isNull();
	}
}
