package org.obd.metrics.api;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.CompletionThread;
import org.obd.metrics.DataCollector;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidRegistry;
import org.obd.metrics.statistics.MetricStatistics;
import org.obd.metrics.statistics.StatisticsRegistry;

public class MultipleDecodersTest {

	@Test
	public void t0() throws IOException, InterruptedException, ExecutionException {
		final Workflow workflow = SimpleWorkflowFactory.getMode01Workflow(new DataCollector());

		final Query query = Query.builder()
		        .pid(22l) // Engine coolant temperature
		        .pid(23l)// Intake manifold absolute pressure
		        .build();

		final MockConnection connection = MockConnection.builder()
		        .commandReply("0100", "4100be3ea813")
		        .commandReply("0200", "4140fed00400")
		        .commandReply("0115", "4115FFff")
		        .build();

		workflow.start(connection, query);

		CompletionThread.setup(workflow);

		final PidRegistry pids = workflow.getPidRegistry();
		PidDefinition pid22 = pids.findBy(22l);
		StatisticsRegistry statistics = workflow.getStatisticsRegistry();
		MetricStatistics stat22 = statistics.findBy(pid22);
		Assertions.assertThat(stat22).isNotNull();

		PidDefinition pid23 = pids.findBy(23l);
		MetricStatistics stat23 = statistics.findBy(pid23);
		Assertions.assertThat(stat23).isNotNull();

		Assertions.assertThat(stat22.getMax()).isEqualTo(10L);
		Assertions.assertThat(stat22.getMin()).isEqualTo(10L);
		Assertions.assertThat(stat22.getMedian()).isEqualTo(10L);

		Assertions.assertThat(stat23.getMax()).isEqualTo(1);
		Assertions.assertThat(stat23.getMin()).isEqualTo(1);
		Assertions.assertThat(stat23.getMedian()).isEqualTo(1);
	}
}
