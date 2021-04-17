package org.obd.metrics.api;

import java.io.IOException;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.WorkflowFinalizer;

public class DeviceErrorTest {

	@Test
	public void errorsTest() throws IOException, InterruptedException {
		var lifecycle = new SimpleLifecycle();

		var workflow = SimpleWorkflowFactory.getMode01Workflow(lifecycle);

		var errors = Map.of(
		        "can Error", "canerror",
		        "bus init", "businit",
		        "STOPPED", "stopped",
		        "ERROR", "error",
		        "Unable To Connect", "unabletoconnect").entrySet();

		for (var input : errors) {
			lifecycle.reset();

			var query = Query.builder()
			        .pid(22l)
			        .pid(23l)
			        .build();

			var connection = MockConnection
			        .builder()
			        .commandReply("ATRV", "12v")
			        .commandReply("0100", "4100be3ea813")
			        .commandReply("0200", "4140fed00400")
			        .commandReply("0115", input.getKey())
			        .build();

			workflow.start(connection, query, Adjustments.builder().initDelay(0).build());

			WorkflowFinalizer.finalizeAfter500ms(workflow);

			Assertions.assertThat(lifecycle.isErrorOccurred()).isTrue();
			Assertions.assertThat(lifecycle.getMessage()).isEqualTo(input.getValue());
		}
	}
}
