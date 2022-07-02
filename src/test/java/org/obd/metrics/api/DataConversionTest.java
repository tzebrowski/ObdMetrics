package org.obd.metrics.api;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;
import org.obd.metrics.connection.SimpleMockConnection;
import org.obd.metrics.pid.PidDefinition;

public class DataConversionTest {

	@Test
	public void typesConversionTest() throws IOException, InterruptedException {

		DataCollector collector = new DataCollector();
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(collector);

		workflow.getPidRegistry()
		        .register(new PidDefinition(10001l, 2, "A + B", "22", "2000", "rpm", "Engine RPM",
		                0, 8000, PidDefinition.ValueType.INT));
		workflow.getPidRegistry()
		        .register(new PidDefinition(10002l, 2, "A + B", "22", "2002", "rpm", "Engine RPM",
		                0, 8000, PidDefinition.ValueType.SHORT));
		workflow.getPidRegistry()
		        .register(new PidDefinition(10003l, 2, "A + B", "22", "2004", "rpm", "Engine RPM",
		                0, 8000, PidDefinition.ValueType.DOUBLE));

		Query query = Query.builder()
		        .pid(10001l)
		        .pid(10002l)
		        .pid(10003l)
		        .build();

		SimpleMockConnection connection = SimpleMockConnection
		        .builder()
		        .commandReply("222000", "6220000BEA")
		        .commandReply("222002", "6220020BEA")
		        .commandReply("222004", "6220040BEA")
		        .build();

		workflow.start(connection, query);

		WorkflowFinalizer.finalizeAfter500ms(workflow);

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
		                PidDefinition.ValueType.DOUBLE));

		Query query = Query.builder()
		        .pid(id)
		        .build();

		SimpleMockConnection connection = SimpleMockConnection.builder()
		        .commandReply("222000", "6220000BEA")
		        .build();

		workflow.start(connection, query);

		WorkflowFinalizer.finalizeAfter500ms(workflow);

		ObdMetric metric = collector.findSingleMetricBy(workflow.getPidRegistry().findBy(id));
		Assertions.assertThat(metric).isNull();
	}

	@Test
	public void noFormulaTest() throws IOException, InterruptedException {
		DataCollector collector = new DataCollector();
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(collector);

		long id = 10001l;
		workflow.getPidRegistry().register(
		        new PidDefinition(id, 2, "", "22", "2000", "rpm", "Engine RPM", 0, 8000, PidDefinition.ValueType.DOUBLE));

		Query query = Query.builder()
		        .pid(id)
		        .build();

		final SimpleMockConnection connection = SimpleMockConnection.builder()
		        .commandReply("222000", "6220000BEA")
		        .build();

		workflow.start(connection, query);

		WorkflowFinalizer.finalizeAfter500ms(workflow);

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
		        		8000, PidDefinition.ValueType.DOUBLE));

		// Query for specified PID's like RPM
		Query query = Query.builder()
		        .pid(id) // Coolant
		        .pid(6008l) // Coolant
		        .pid(6004l) // RPM
		        .pid(6007l) // Intake temp
		        .pid(6015l)// Oil temp
		        .pid(603l) // Spark Advance
		        .build();

		SimpleMockConnection connection = SimpleMockConnection.builder()
		        .commandReply("222000", "xxxxxxxxxxxxxx")
		        .commandReply("221000", "")
		        .commandReply("221935", "nodata")
		        .commandReply("22194f", "stopped")
		        .commandReply("221812", "unabletoconnect")
		        .build();

		workflow.start(connection, query);

		WorkflowFinalizer.finalizeAfter500ms(workflow);

		ObdMetric metric = collector.findSingleMetricBy(workflow.getPidRegistry().findBy(id));
		Assertions.assertThat(metric).isNull();
	}
}
