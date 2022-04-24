package org.obd.metrics.api;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;
import org.obd.metrics.connection.SimpleMockConnection;
import org.obd.metrics.diagnostic.Histogram;
import org.obd.metrics.diagnostic.RateType;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;

public class StatisticsTest {

	@Test
	public void mode01WorkflowTest() throws IOException, InterruptedException {

		DataCollector collector = new DataCollector();
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(collector);

		Query query = Query.builder()
		        .pid(6l) // Engine coolant temperature
		        .pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        .pid(16l) // Intake air temperature
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .build();

		SimpleMockConnection connection = SimpleMockConnection.builder()
		        .commandReply("0100", "4100be3ea813")
		        .commandReply("0200", "4140fed00400")
		        .commandReply("01 0B 0C 11 0D 0F 05", "00e0:410bff0c00001:11000d000f00052:00aaaaaaaaaaaa")
		        .build();

		Adjustments optional = Adjustments.builder()
		        .batchEnabled(true)
		        .build();

		workflow.start(connection, query, optional);

		WorkflowFinalizer.finalizeAfter(workflow, 1000l);

		PidDefinitionRegistry pids = workflow.getPidRegistry();

		PidDefinition engineTemp = pids.findBy(6l);
		Assertions.assertThat(engineTemp.getPid()).isEqualTo("05");

		Assertions.assertThat(workflow.getDiagnostics().rate(). findBy(RateType.MEAN, engineTemp).get().getValue()).isGreaterThan(10d);
		Assertions.assertThat(workflow.getDiagnostics().rate().findBy(RateType.MEAN, pids.findBy(12l)).get().getValue()).isGreaterThan(10d);
	}

	@Test
	public void genericWorkflowTest() throws IOException, InterruptedException {

		DataCollector collector = new DataCollector();
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(collector);

		Query query = Query.builder()
		        .pid(6008l) // Coolant
		        .pid(6004l) // RPM
		        .pid(6007l) // Intake temp
		        .pid(6015l)// Oil temp
		        .pid(6003l) // Spark Advance
		        .build();

		SimpleMockConnection connection = SimpleMockConnection.builder()
		        .commandReply("221003", "62100340")
		        .commandReply("221000", "6210000BEA")
		        .commandReply("221935", "62193540")
		        .commandReply("22194f", "62194f2d85")
		        .build();

		workflow.start(connection, query, Adjustments.builder().build());

		WorkflowFinalizer.finalizeAfter500ms(workflow);

		PidDefinitionRegistry pids = workflow.getPidRegistry();

		PidDefinition pid8l = pids.findBy(6008l);
		Histogram stat8l = workflow.getDiagnostics().histogram().findBy(pid8l);
		Assertions.assertThat(stat8l).isNotNull();

		PidDefinition pid4l = pids.findBy(6004l);
		Histogram stat4L = workflow.getDiagnostics().histogram().findBy(pid4l);
		Assertions.assertThat(stat4L).isNotNull();

		Assertions.assertThat(stat4L.getMax()).isEqualTo(762.5);
		Assertions.assertThat(stat4L.getMin()).isEqualTo(762.5);
		Assertions.assertThat(stat4L.getMean()).isEqualTo(762.5);

		Assertions.assertThat(stat8l.getMax()).isEqualTo(-1);
		Assertions.assertThat(stat8l.getMin()).isEqualTo(-1);
		Assertions.assertThat(stat8l.getMean()).isEqualTo(-1);

		Assertions.assertThat(workflow.getDiagnostics().rate().findBy(RateType.MEAN, pid8l).get().getValue()).isGreaterThan(10d);
		Assertions.assertThat(workflow.getDiagnostics().rate().findBy(RateType.MEAN, pid4l).get().getValue()).isGreaterThan(10d);
	}
}
