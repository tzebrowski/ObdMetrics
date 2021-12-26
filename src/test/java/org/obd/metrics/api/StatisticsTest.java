package org.obd.metrics.api;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidRegistry;
import org.obd.metrics.statistics.MetricStatistics;

public class StatisticsTest {

	@Test
	public void mode01WorkflowTest() throws IOException, InterruptedException {

		DataCollector collector = new DataCollector();
		Workflow workflow = SimpleWorkflowFactory.getMode01Workflow(collector);

		Query query = Query.builder()
		        .pid(6l) // Engine coolant temperature
		        .pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        .pid(16l) // Intake air temperature
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .build();

		MockConnection connection = MockConnection.builder()
		        .commandReply("0100", "4100be3ea813")
		        .commandReply("0200", "4140fed00400")
		        .commandReply("01 0B 0C 11 0D 0F 05", "00e0:410bff0c00001:11000d000f00052:00aaaaaaaaaaaa")
		        .build();

		Adjustments optional = Adjustments.builder()
		        .initDelay(0)
		        .batchEnabled(true)
		        .build();

		workflow.start(connection, query, optional);

		WorkflowFinalizer.finalizeAfter(workflow,1000l);

		PidRegistry pids = workflow.getPidRegistry();

		PidDefinition engineTemp = pids.findBy(6l);
		Assertions.assertThat(engineTemp.getPid()).isEqualTo("05");

		Assertions.assertThat(workflow.getStatisticsRegistry().getRatePerSec(engineTemp)).isGreaterThan(10d);
		Assertions.assertThat(workflow.getStatisticsRegistry().getRatePerSec(pids.findBy(12l))).isGreaterThan(10d);
	}

	@Test
	public void genericWorkflowTest() throws IOException, InterruptedException {

		DataCollector collector = new DataCollector();
		Workflow workflow = SimpleWorkflowFactory.getMode22Workflow(collector);

		Query query = Query.builder()
		        .pid(8l) // Coolant
		        .pid(4l) // RPM
		        .pid(7l) // Intake temp
		        .pid(15l)// Oil temp
		        .pid(3l) // Spark Advance
		        .build();

		MockConnection connection = MockConnection.builder()
		        .commandReply("221003", "62100340")
		        .commandReply("221000", "6210000BEA")
		        .commandReply("221935", "62193540")
		        .commandReply("22194f", "62194f2d85")
		        .build();

		workflow.start(connection, query, Adjustments.builder().initDelay(0).build());

		WorkflowFinalizer.finalizeAfter500ms(workflow);

		PidRegistry pids = workflow.getPidRegistry();

		PidDefinition pid8l = pids.findBy(8l);
		MetricStatistics stat8l = workflow.getStatisticsRegistry().findBy(pid8l);
		Assertions.assertThat(stat8l).isNotNull();

		PidDefinition pid4l = pids.findBy(4l);
		MetricStatistics stat4L = workflow.getStatisticsRegistry().findBy(pid4l);
		Assertions.assertThat(stat4L).isNotNull();

		Assertions.assertThat(stat4L.getMax()).isEqualTo(762);
		Assertions.assertThat(stat4L.getMin()).isEqualTo(762);
		Assertions.assertThat(stat4L.getMedian()).isEqualTo(762);

		Assertions.assertThat(stat8l.getMax()).isEqualTo(-1);
		Assertions.assertThat(stat8l.getMin()).isEqualTo(-1);
		Assertions.assertThat(stat8l.getMedian()).isEqualTo(-1);

		Assertions.assertThat(workflow.getStatisticsRegistry().getRatePerSec(pid8l)).isGreaterThan(10d);
		Assertions.assertThat(workflow.getStatisticsRegistry().getRatePerSec(pid4l)).isGreaterThan(10d);
	}
}
