package org.obd.metrics.api;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class MultipleDecodersTest {

	@Test
	public void t0() throws IOException, InterruptedException, ExecutionException {
		var workflow = SimpleWorkflowFactory.getMode01Workflow();

		var query = Query.builder()
		        .pid(22l) // Engine coolant temperature
		        .pid(23l)// Intake manifold absolute pressure
		        .build();

		var connection = MockConnection.builder()
		        .commandReply("0100", "4100be3ea813")
		        .commandReply("0200", "4140fed00400")
		        .commandReply("0115", "4115FFff")
		        .build();

		workflow.start(connection, query,Adjustments.builder().initDelay(0).build());


		WorkflowFinalizer.finalizeAfter500ms(workflow);

		var pids = workflow.getPidRegistry();
		var pid22 = pids.findBy(22l);
		var statistics = workflow.getStatisticsRegistry();
		var stat22 = statistics.findBy(pid22);
		Assertions.assertThat(stat22).isNotNull();

		var pid23 = pids.findBy(23l);
		var stat23 = statistics.findBy(pid23);
		Assertions.assertThat(stat23).isNotNull();

		Assertions.assertThat(stat22.getMax()).isEqualTo(10L);
		Assertions.assertThat(stat22.getMin()).isEqualTo(10L);
		Assertions.assertThat(stat22.getMedian()).isEqualTo(10L);

		Assertions.assertThat(stat23.getMax()).isEqualTo(1);
		Assertions.assertThat(stat23.getMin()).isEqualTo(1);
		Assertions.assertThat(stat23.getMedian()).isEqualTo(1);
	}
}
