package org.obd.metrics.api;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.diagnostic.Diagnostics;
import org.obd.metrics.diagnostic.Histogram;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;

public class MultipleDecodersTest {

	@Test
	public void t0() throws IOException, InterruptedException, ExecutionException {
		Workflow workflow = SimpleWorkflowFactory.getMode01Workflow();

		Query query = Query.builder()
		        .pid(22l)
		        .pid(23l)
		        .build();

		MockConnection connection = MockConnection.builder()
		        .commandReply("0100", "4100be3ea813")
		        .commandReply("0200", "4140fed00400")
		        .commandReply("0115", "4115FFff")
		        .build();

		workflow.start(connection, query,Adjustments.builder().initDelay(0).build());


		WorkflowFinalizer.finalizeAfter(workflow,1000);

		PidDefinitionRegistry pids = workflow.getPidRegistry();
		PidDefinition pid22 = pids.findBy(22l);
		Diagnostics statistics = workflow.getDiagnostics();
		Histogram stat22 = statistics.findHistogramBy(pid22);
		Assertions.assertThat(stat22).isNotNull();

		PidDefinition pid23 = pids.findBy(23l);
		Histogram stat23 = statistics.findHistogramBy(pid23);
		Assertions.assertThat(stat23).isNotNull();
		System.out.println("MultipleDecodersTest.t0() : " + stat22.getMax());	
		Assertions.assertThat(stat22.getMax()).isEqualTo(10.51);
		Assertions.assertThat(stat22.getMin()).isEqualTo(10.51);

		Assertions.assertThat(stat23.getMax()).isEqualTo(1.27);
		Assertions.assertThat(stat23.getMin()).isEqualTo(1.27);
	}
}
