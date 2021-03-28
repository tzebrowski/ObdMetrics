package org.obd.metrics.api;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.WorkflowFinalizer;

public class DeviceErrorTest {

	@Test
	public void errorsTest() throws IOException, InterruptedException {
		final LifecycleImpl lifecycle = new LifecycleImpl();

		final Workflow workflow = SimpleWorkflowFactory.getMode01Workflow(lifecycle);

		final Set<Entry<String, String>> errors = Map.of(
		        "can Error", "canerror",
		        "bus init", "businit",
		        "STOPPED", "stopped",
		        "ERROR", "error",
		        "Unable To Connect", "unabletoconnect").entrySet();

		for (final Map.Entry<String, String> input : errors) {
			lifecycle.reset();

			final Set<Long> filter = new HashSet<>();
			filter.add(22l);
			filter.add(23l);

			MockConnection connection = MockConnection
			        .builder()
			        .commandReply("ATRV", "12v")
			        .commandReply("0100", "4100be3ea813")
			        .commandReply("0200", "4140fed00400")
			        .commandReply("0115", input.getKey())
			        .build();

			workflow.start(connection,Query.builder().pids(filter).build());

			WorkflowFinalizer.finalizeAfter500ms(workflow);


			Assertions.assertThat(lifecycle.isErrorOccurred()).isTrue();
			Assertions.assertThat(lifecycle.getMessage()).isEqualTo(input.getValue());
		}
	}
}
