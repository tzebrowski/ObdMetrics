package org.obd.metrics.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.connection.MockAdapterConnection;

public class DeviceErrorTest {

	@SuppressWarnings("serial")
	@Test
	public void errorsTest() throws IOException, InterruptedException {
		SimpleLifecycle lifecycle = new SimpleLifecycle();

		Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle);

		Map<String, String> errors = new HashMap<String, String>() {
			{
				put("can Error", "CANERROR");
				put("bus init", "BUSINIT");
				put("STOPPED", "STOPPED");
				put("ERROR", "ERROR");
				put("Unable To Connect", "UNABLETOCONNECT");
			}
		};

		for (final Entry<String, String> input : errors.entrySet()) {
			lifecycle.reset();

			Query query = Query.builder()
			        .pid(22l)
			        .pid(23l)
			        .build();

			MockAdapterConnection connection = MockAdapterConnection
			        .builder()
			        .requestResponse("ATRV", "12v")
			        .requestResponse("0100", "4100BE3EA813")
			        .requestResponse("0200", "4140FED00400")
			        .requestResponse("0115", input.getKey())
			        .build();

			workflow.start(connection, query);

			WorkflowFinalizer.finalize(workflow);

			Assertions.assertThat(lifecycle.isErrorOccurred()).isTrue();
			Assertions.assertThat(lifecycle.getMessage()).isEqualTo(input.getValue());
		}
	}
}
