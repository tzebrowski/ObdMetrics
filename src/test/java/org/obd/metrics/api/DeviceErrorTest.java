package org.obd.metrics.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class DeviceErrorTest {

	@Test
	public void errorsTest() throws IOException, InterruptedException {
		SimpleLifecycle lifecycle = new SimpleLifecycle();

		Workflow workflow = SimpleWorkflowFactory.getMode01Workflow(lifecycle);

		Map<String, String> errors = new HashMap<String, String>() {
			{
				put("can Error", "canerror");
				put("bus init", "businit");
				put("STOPPED", "stopped");
				put("ERROR", "error");
				put("Unable To Connect", "unabletoconnect");
			}
		};

		for (final Entry<String, String> input : errors.entrySet()) {
			lifecycle.reset();

			Query query = Query.builder()
			        .pid(22l)
			        .pid(23l)
			        .build();

			MockConnection connection = MockConnection
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
