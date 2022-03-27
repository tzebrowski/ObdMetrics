package org.obd.metrics.api;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;
import org.obd.metrics.ObdMetric;
import org.obd.metrics.codec.GeneratorSpec;
import org.obd.metrics.diagnostic.Histogram;
import org.obd.metrics.diagnostic.RateType;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataGeneratorTest {

	@Test
	public void generatorTest() throws IOException, InterruptedException {
		Workflow workflow = SimpleWorkflowFactory.getMode22Workflow();

		Query query = Query.builder()
		        .pid(8l) // Coolant
		        .pid(4l) // RPM
		        .pid(7l) // Intake temp
		        .pid(15l)// Oil temp
		        .pid(3l) // Spark Advance
		        .build();

		MockConnection connection = MockConnection.builder()
		        .commandReply("221003", "62100340")
		        .commandReply("221000", "xxxxxxxxxxxxxx")
		        .commandReply("221935", "xxxxxxxxxxxxxx")
		        .commandReply("22194f", "xxxxxxxxxxxxxx")
		        .commandReply("221812", "")
		        .build();

		Adjustments optional = Adjustments
		        .builder()
		        .initDelay(0)
		        .generator(GeneratorSpec.builder().increment(5.0).enabled(true).build())
		        .build();

		workflow.start(connection, query, optional);

		WorkflowFinalizer.finalizeAfter(workflow,1000);

		PidDefinitionRegistry pids = workflow.getPidRegistry();

		PidDefinition pid8l = pids.findBy(8l);

		Assertions.assertThat(workflow.getDiagnostics().rate().findBy(RateType.MEAN,pid8l).get().getValue()).isGreaterThan(0);

		Histogram histogram = workflow.getDiagnostics().histogram().findBy(pid8l);
		log.info(histogram.getMax() + ". " + histogram.getMin() + " " + histogram.getMean());
		Assertions.assertThat(histogram.getMax()).isGreaterThan(histogram.getMin());
		Assertions.assertThat(histogram.getMin()).isLessThan((long) histogram.getMean());
		Assertions.assertThat(histogram.getMean()).isLessThan(histogram.getMax()).isGreaterThan(histogram.getMin());
	}

	@Test
	public void defaultIncrementTest() throws IOException, InterruptedException {

		Workflow workflow = SimpleWorkflowFactory.getMode22Workflow();

		Query query = Query.builder()
		        .pid(8l) // Coolant
		        .pid(4l) // RPM
		        .pid(7l) // Intake temp
		        .pid(15l)// Oil temp
		        .pid(3l) // Spark Advance
		        .build();

		MockConnection connection = MockConnection.builder()
		        .commandReply("221003", "62100340")
		        .commandReply("221000", "xxxxxxxxxxxxxx")
		        .commandReply("221935", "xxxxxxxxxxxxxx")
		        .commandReply("22194f", "xxxxxxxxxxxxxx")
		        .commandReply("221812", "")
		        .build();

		Adjustments optional = Adjustments
		        .builder()
		        .initDelay(0)
		        .generator(GeneratorSpec.builder().enabled(true).build())
		        .build();

		workflow.start(connection, query, optional);

		WorkflowFinalizer.finalizeAfter(workflow,1000);

		PidDefinitionRegistry pids = workflow.getPidRegistry();

		PidDefinition pid8l = pids.findBy(8l);

		Assertions.assertThat(workflow.getDiagnostics().rate().findBy(RateType.MEAN,pid8l).get().getValue()).isGreaterThan(0);

		Histogram histogram = workflow.getDiagnostics().histogram().findBy(pid8l);

		Assertions.assertThat(histogram.getMax()).isGreaterThan(histogram.getMin());
		Assertions.assertThat(histogram.getMin()).isLessThan((long) histogram.getMean());
		Assertions.assertThat(histogram.getMean()).isLessThan(histogram.getMax()).isGreaterThan(histogram.getMin());
	}

	@Test
	public void smartTest() throws IOException, InterruptedException {

		DataCollector collector = new DataCollector();
		Workflow workflow = SimpleWorkflowFactory.getMode22Workflow(collector);

		PidDefinitionRegistry pidRegistry = workflow.getPidRegistry();
		pidRegistry.register(new PidDefinition(10001l, 2, "((A *256 ) +B)/4", "22", "2000", "rpm", "Engine RPM",
		        0, 8000, PidDefinition.ValueType.DOUBLE));
		pidRegistry.register(new PidDefinition(10002l, 2, "((A *256 ) +B)/4", "22", "2002", "rpm", "Engine RPM",
		        2, 8000, PidDefinition.ValueType.DOUBLE));
		pidRegistry.register(new PidDefinition(10003l, 2, "((A *256 ) +B)/4", "22", "2004", "rpm", "Engine RPM",
		        5, 8000, PidDefinition.ValueType.DOUBLE));

		pidRegistry.register(new PidDefinition(10004l, 2, "((A *256 ) +B)/4", "22", "2006", "rpm", "Engine RPM",
		        20, 100, PidDefinition.ValueType.DOUBLE));

		pidRegistry.register(new PidDefinition(10005l, 2, "((A *256 ) +B)/4", "22", "2008", "rpm", "Engine RPM",
		        0, 7000, PidDefinition.ValueType.DOUBLE));

		Query query = Query.builder()
		        .pid(10001l) // Coolant
		        .pid(10002l) // RPM
		        .pid(10003l) // Intake temp
		        .pid(10004l) // Oil temp
		        .pid(10005l) // Spark Advance
		        .build();

		MockConnection connection = MockConnection.builder()
		        .commandReply("222000", "6220000BEA")
		        .commandReply("222002", "6220020BEA")
		        .commandReply("222004", "6220040BEA")
		        .commandReply("222006", "6220060BEA")
		        .commandReply("222008", "6220080BEA")
		        .build();

		Adjustments optional = Adjustments.builder()
		        .initDelay(0)
				.generator(GeneratorSpec.builder().smart(true).enabled(true).build())
		        .build();

		workflow.start(connection, query, optional);

		WorkflowFinalizer.finalizeAfter500ms(workflow);

		ObdMetric metric = collector.findSingleMetricBy(workflow.getPidRegistry().findBy(10002l));
		Assertions.assertThat(metric.getValue()).isNotNull().isInstanceOf(Double.class);

		metric = collector.findSingleMetricBy(workflow.getPidRegistry().findBy(10001l));
		Assertions.assertThat(metric.getValue()).isNotNull().isInstanceOf(Double.class);

		metric = collector.findSingleMetricBy(workflow.getPidRegistry().findBy(10003l));
		Assertions.assertThat(metric.getValue()).isNotNull().isInstanceOf(Double.class);
	}
}
