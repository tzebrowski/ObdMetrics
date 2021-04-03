package org.obd.metrics.api;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.WorkflowFinalizer;

public class ConnectorTest {

	@Test
	public void characterTest() throws IOException, InterruptedException {
		var lifecycle = new LifecycleImpl();

		var workflow = SimpleWorkflowFactory.getMode01Workflow(lifecycle);

		var query = Query.builder()
		        .pid(22l)
		        .pid(23l)
		        .build();

		var connection = MockConnection.builder()
		        .commandReply("09 02", "SEARCHING...0140:4902015756571:5A5A5A314B5A412:4D363930333932")
		        .commandReply("0100", "\r4100be3ea813")
		        .commandReply("0200", "4140fed00400\n")
		        .commandReply("0115", "\t4 1 1 5 F F f f>\r")
		        .build();

		workflow.start(connection, query, Adjustments.builder().initDelay(0).build());

		WorkflowFinalizer.finalizeAfter500ms(workflow);

		Assertions.assertThat(lifecycle.isErrorOccurred()).isFalse();

		var ratePerSec = workflow.getStatisticsRegistry().getRatePerSec(workflow.getPidRegistry().findBy("15"));
		Assertions.assertThat(ratePerSec).isGreaterThan(0);
	}

	@Test
	public void readErrorTest() throws IOException, InterruptedException {
		var lifecycle = new LifecycleImpl();

		var workflow = SimpleWorkflowFactory.getMode01Workflow(lifecycle);

		var query = Query.builder()
		        .pid(22l)
		        .pid(23l)
		        .build();

		var connection = MockConnection.builder()
		        .commandReply("0100", "4100be3ea813")
		        .commandReply("0200", "4140fed00400")
		        .commandReply("0115", "4115FFff")
		        .simulateReadError(true) // simulate read error
		        .build();

		workflow.start(connection, query);

		WorkflowFinalizer.finalizeAfter500ms(workflow);

		Assertions.assertThat(lifecycle.isErrorOccurred()).isTrue();
	}

	@Test
	public void reconnectErrorTest() throws IOException, InterruptedException {
		var lifecycle = new LifecycleImpl();

		var workflow = SimpleWorkflowFactory.getMode01Workflow(lifecycle);

		var query = Query.builder()
		        .pid(22l)
		        .pid(23l)
		        .build();

		var connection = MockConnection.builder()
		        .commandReply("0100", "4100be3ea813")
		        .commandReply("0200", "4140fed00400")
		        .commandReply("0115", "4115FFff")
		        .simulateErrorInReconnect(true)
		        .simulateWriteError(true) // simulate write error
		        .build();

		workflow.start(connection, query, Adjustments.builder().initDelay(0).build());

		WorkflowFinalizer.finalizeAfter500ms(workflow);

		Assertions.assertThat(lifecycle.isErrorOccurred()).isTrue();
	}
}
