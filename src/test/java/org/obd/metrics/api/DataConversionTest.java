package org.obd.metrics.api;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.WorkflowFinalizer;
import org.obd.metrics.DataCollector;
import org.obd.metrics.ObdMetric;
import org.obd.metrics.pid.PidDefinition;


public class DataConversionTest {

	@Test
	public void typesConversionTest() throws IOException, InterruptedException {

		final DataCollector collector = new DataCollector();
		final Workflow workflow = SimpleWorkflowFactory.getMode22Workflow(collector);

		workflow.getPidRegistry()
		        .register(new PidDefinition(10001l, 2, "((A *256 ) +B)/4", "22", "2000", "rpm", "Engine RPM",
		                0, 100, PidDefinition.Type.INT));
		workflow.getPidRegistry()
		        .register(new PidDefinition(10002l, 2, "((A *256 ) +B)/4", "22", "2002", "rpm", "Engine RPM",
		                0, 100, PidDefinition.Type.SHORT));
		workflow.getPidRegistry()
		        .register(new PidDefinition(10003l, 2, "((A *256 ) +B)/4", "22", "2004", "rpm", "Engine RPM",
		                0, 100, PidDefinition.Type.DOUBLE));

		final Set<Long> ids = new HashSet<>();
		ids.add(10001l);
		ids.add(10002l);
		ids.add(10003l);

		final MockConnection connection = MockConnection.builder()
		        .commandReply("222000", "6220000BEA")
		        .commandReply("222002", "6220020BEA")
		        .commandReply("222004", "6220040BEA")
		        .build();

		workflow.start(connection,Query.builder().pids(ids).build());

		WorkflowFinalizer.finalizeAfter500ms(workflow);

		List<ObdMetric> collection = collector.findMetricsBy(workflow.getPidRegistry().findBy(10002l));
		Assertions.assertThat(collection.isEmpty()).isFalse();
		Assertions.assertThat(collection.iterator().next().getValue()).isInstanceOf(Short.class);

		collection = collector.findMetricsBy(workflow.getPidRegistry().findBy(10001l));
		Assertions.assertThat(collection.isEmpty()).isFalse();
		Assertions.assertThat(collection.iterator().next().getValue()).isInstanceOf(Integer.class);

		collection = collector.findMetricsBy(workflow.getPidRegistry().findBy(10003l));
		Assertions.assertThat(collection.isEmpty()).isFalse();
		Assertions.assertThat(collection.iterator().next().getValue()).isInstanceOf(Double.class);
	}

	@Test
	public void invalidFormulaTest() throws IOException, InterruptedException {
		final DataCollector collector = new DataCollector();
		final Workflow workflow = SimpleWorkflowFactory.getMode22Workflow(collector);

		long id = 10001l;

		final String invalidFormula = "(A *256 ) +B )/4";
		workflow.getPidRegistry()
		        .register(new PidDefinition(id, 2, invalidFormula, "22", "2000", "rpm", "Engine RPM", 0, 100,
		                PidDefinition.Type.DOUBLE));

		final Set<Long> ids = new HashSet<>();
		ids.add(id);

		final MockConnection connection = MockConnection.builder()
		        .commandReply("222000", "6220000BEA")
		        .build();

		workflow.start(connection,Query.builder().pids(ids).build());

		WorkflowFinalizer.finalizeAfter500ms(workflow);


		final List<ObdMetric> collection = collector.findMetricsBy(workflow.getPidRegistry().findBy(id));
		Assertions.assertThat(collection.isEmpty()).isFalse();
		Assertions.assertThat(collection.iterator().next().getValue()).isNull();
	}

	@Test
	public void noFormulaTest() throws IOException, InterruptedException {
		final DataCollector collector = new DataCollector();
		final Workflow workflow = SimpleWorkflowFactory.getMode22Workflow(collector);

		long id = 10001l;
		workflow.getPidRegistry().register(
		        new PidDefinition(id, 2, "", "22", "2000", "rpm", "Engine RPM", 0, 100, PidDefinition.Type.DOUBLE));

		final Set<Long> ids = new HashSet<>();
		ids.add(id);

		final MockConnection connection = MockConnection.builder()
		        .commandReply("222000", "6220000BEA")
		        .build();

		workflow.start(connection,Query.builder().pids(ids).build());
		
		WorkflowFinalizer.finalizeAfter500ms(workflow);

		final List<ObdMetric> collection = collector.findMetricsBy(workflow.getPidRegistry().findBy(id));
		Assertions.assertThat(collection.isEmpty()).isFalse();
		Assertions.assertThat(collection.iterator().next().getValue()).isNull();

	}

	@Test
	public void invalidaDataTest() throws IOException, InterruptedException {
		final DataCollector collector = new DataCollector();
		final Workflow workflow = SimpleWorkflowFactory.getMode22Workflow(collector);

		long id = 10001l;
		workflow.getPidRegistry()
		        .register(new PidDefinition(id, 2, "(A *256 ) +B )/4", "22", "2000", "rpm", "Engine RPM", 0,
		                100, PidDefinition.Type.DOUBLE));

		final Set<Long> ids = new HashSet<>();
		ids.add(id); // Coolant
		ids.add(8l); // Coolant
		ids.add(4l); // RPM
		ids.add(7l); // Intake temp
		ids.add(15l);// Oil temp
		ids.add(3l); // Spark Advance

		final MockConnection connection = MockConnection.builder()
		        .commandReply("222000", "xxxxxxxxxxxxxx")
		        .commandReply("221000", "")
		        .commandReply("221935", "nodata")
		        .commandReply("22194f", "stopped")
		        .commandReply("221812", "unabletoconnect")
		        .build();

		workflow.start(connection,Query.builder().pids(ids).build());

		WorkflowFinalizer.finalizeAfter500ms(workflow);

		final List<ObdMetric> collection = collector.findMetricsBy(workflow.getPidRegistry().findBy(id));
		Assertions.assertThat(collection.isEmpty()).isFalse();
		Assertions.assertThat(collection.iterator().next().getValue()).isNull();
	}
}
