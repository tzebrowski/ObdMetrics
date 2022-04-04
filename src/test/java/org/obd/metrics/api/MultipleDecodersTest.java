package org.obd.metrics.api;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.connection.SimpleMockConnection;
import org.obd.metrics.diagnostic.Diagnostics;
import org.obd.metrics.diagnostic.Histogram;
import org.obd.metrics.pid.PidDefinitionRegistry;

public class MultipleDecodersTest {

	@Test
	public void t0() throws IOException, InterruptedException, ExecutionException {
		Workflow workflow = SimpleWorkflowFactory.getMode01Workflow();

		Query query = Query.builder()
		        .pid(22l)
		        .pid(23l)
		        .build();

		SimpleMockConnection connection = SimpleMockConnection.builder()
		        .commandReply("0100", "4100be3ea813")
		        .commandReply("0200", "4140fed00400")
		        .commandReply("0115", "4115FFff")
		        .build();

		workflow.start(connection, query,Adjustments.builder().initDelay(0).build());


		WorkflowFinalizer.finalizeAfter(workflow,1000);

		PidDefinitionRegistry pids = workflow.getPidRegistry();
		Diagnostics diagnostics = workflow.getDiagnostics();
		

		Histogram histogram = diagnostics.histogram().findBy(pids.findBy(22l));
		Assertions.assertThat(histogram).isNotNull();
		Assertions.assertThat(histogram.getMax()).isEqualTo(10.51);
		Assertions.assertThat(histogram.getMin()).isEqualTo(10.51);
		
		histogram = diagnostics.histogram().findBy(pids.findBy(23l));
		Assertions.assertThat(histogram).isNotNull();
		Assertions.assertThat(histogram.getMax()).isEqualTo(1.27);
		Assertions.assertThat(histogram.getMin()).isEqualTo(1.27);

	}
}
