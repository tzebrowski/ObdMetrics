package org.obd.metrics.api;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;
import org.obd.metrics.WorkflowFinalizer;
import org.obd.metrics.pid.PidDefinition;

public class DataConversionTest {

	@Test
	public void typesConversionTest() throws IOException, InterruptedException {

		var collector = new DataCollector();
		var workflow = SimpleWorkflowFactory.getMode22Workflow(collector);

		workflow.getPidRegistry()
		        .register(new PidDefinition(10001l, 2, "((A *256 ) +B)/4", "22", "2000", "rpm", "Engine RPM",
		                0, 100, PidDefinition.Type.INT));
		workflow.getPidRegistry()
		        .register(new PidDefinition(10002l, 2, "((A *256 ) +B)/4", "22", "2002", "rpm", "Engine RPM",
		                0, 100, PidDefinition.Type.SHORT));
		workflow.getPidRegistry()
		        .register(new PidDefinition(10003l, 2, "((A *256 ) +B)/4", "22", "2004", "rpm", "Engine RPM",
		                0, 100, PidDefinition.Type.DOUBLE));

		var query = Query.builder()
		        .pid(10001l)
		        .pid(10002l)
		        .pid(10003l)
		        .build();

		var connection = MockConnection
		        .builder()
		        .commandReply("222000", "6220000BEA")
		        .commandReply("222002", "6220020BEA")
		        .commandReply("222004", "6220040BEA")
		        .build();

		workflow.start(connection, query);

		WorkflowFinalizer.finalizeAfter500ms(workflow);

		var metric = collector.findSingleMetricBy(workflow.getPidRegistry().findBy(10002l));
		Assertions.assertThat(metric.getValue()).isNotNull().isInstanceOf(Short.class);

		metric = collector.findSingleMetricBy(workflow.getPidRegistry().findBy(10001l));
		Assertions.assertThat(metric.getValue()).isNotNull().isInstanceOf(Integer.class);

		metric = collector.findSingleMetricBy(workflow.getPidRegistry().findBy(10003l));
		Assertions.assertThat(metric.getValue()).isNotNull().isInstanceOf(Double.class);
	}

	@Test
	public void invalidFormulaTest() throws IOException, InterruptedException {
		var collector = new DataCollector();
		var workflow = SimpleWorkflowFactory.getMode22Workflow(collector);

		var id = 10001l;

		var invalidFormula = "(A *256 ) +B )/4";
		workflow.getPidRegistry()
		        .register(new PidDefinition(id, 2, invalidFormula, "22", "2000", "rpm", "Engine RPM", 0, 100,
		                PidDefinition.Type.DOUBLE));

		var query = Query.builder()
		        .pid(id)
		        .build();

		var connection = MockConnection.builder()
		        .commandReply("222000", "6220000BEA")
		        .build();

		workflow.start(connection, query);

		WorkflowFinalizer.finalizeAfter500ms(workflow);

		var metric = collector.findSingleMetricBy(workflow.getPidRegistry().findBy(id));
		Assertions.assertThat(metric).isNotNull();
		Assertions.assertThat(metric.getValue()).isNull();
	}

	@Test
	public void noFormulaTest() throws IOException, InterruptedException {
		var collector = new DataCollector();
		var workflow = SimpleWorkflowFactory.getMode22Workflow(collector);

		var id = 10001l;
		workflow.getPidRegistry().register(
		        new PidDefinition(id, 2, "", "22", "2000", "rpm", "Engine RPM", 0, 100, PidDefinition.Type.DOUBLE));

		var query = Query.builder()
		        .pid(id)
		        .build();

		final MockConnection connection = MockConnection.builder()
		        .commandReply("222000", "6220000BEA")
		        .build();

		workflow.start(connection, query);

		WorkflowFinalizer.finalizeAfter500ms(workflow);

		var metric = collector.findSingleMetricBy(workflow.getPidRegistry().findBy(id));
		Assertions.assertThat(metric).isNotNull();
		Assertions.assertThat(metric.getValue()).isNull();

	}

	@Test
	public void invalidaDataTest() throws IOException, InterruptedException {
		var collector = new DataCollector();
		var workflow = SimpleWorkflowFactory.getMode22Workflow(collector);

		var id = 10001l;
		workflow.getPidRegistry()
		        .register(new PidDefinition(id, 2, "(A *256 ) +B )/4", "22", "2000", "rpm", "Engine RPM", 0,
		                100, PidDefinition.Type.DOUBLE));

		// Query for specified PID's like RPM
		var query = Query.builder()
		        .pid(id) // Coolant
		        .pid(8l) // Coolant
		        .pid(4l) // RPM
		        .pid(7l) // Intake temp
		        .pid(15l)// Oil temp
		        .pid(3l) // Spark Advance
		        .build();

		var connection = MockConnection.builder()
		        .commandReply("222000", "xxxxxxxxxxxxxx")
		        .commandReply("221000", "")
		        .commandReply("221935", "nodata")
		        .commandReply("22194f", "stopped")
		        .commandReply("221812", "unabletoconnect")
		        .build();

		workflow.start(connection, query);

		WorkflowFinalizer.finalizeAfter500ms(workflow);

		var metric = collector.findSingleMetricBy(workflow.getPidRegistry().findBy(id));
		Assertions.assertThat(metric).isNotNull();
		Assertions.assertThat(metric.getValue()).isNull();
	}
}
