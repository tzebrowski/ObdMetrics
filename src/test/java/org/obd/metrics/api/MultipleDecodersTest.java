package org.obd.metrics.api;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.connection.MockAdapterConnection;
import org.obd.metrics.diagnostic.Diagnostics;
import org.obd.metrics.diagnostic.Histogram;
import org.obd.metrics.pid.PidDefinitionRegistry;

public class MultipleDecodersTest {

	@Test
	public void t0() throws IOException, InterruptedException, ExecutionException {
		Workflow workflow = SimpleWorkflowFactory.getWorkflow();

		Query query = Query.builder()
		        .pid(22l)
		        .build();

		MockAdapterConnection connection = MockAdapterConnection.builder()
		        .requestResponse("0100", "4100be3ea813")
		        .requestResponse("0200", "4140fed00400")
		        .requestResponse("0115", "4115FFff")
		        .build();

		workflow.start(connection, query);


		WorkflowFinalizer.finalizeAfter(workflow,1000);

		PidDefinitionRegistry pids = workflow.getPidRegistry();
		Diagnostics diagnostics = workflow.getDiagnostics();
		

		Histogram histogram = diagnostics.histogram().findBy(pids.findBy(22l));
		Assertions.assertThat(histogram).isNotNull();
		Assertions.assertThat(histogram.getMax()).isEqualTo(10.51);
		Assertions.assertThat(histogram.getMin()).isEqualTo(10.51);
	}
}
