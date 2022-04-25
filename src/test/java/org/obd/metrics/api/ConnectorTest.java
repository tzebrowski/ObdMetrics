package org.obd.metrics.api;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.connection.SimpleMockConnection;
import org.obd.metrics.diagnostic.RateType;
import org.obd.metrics.pid.PidDefinition;

public class ConnectorTest {

	@Test
	public void characterTest() throws IOException, InterruptedException {
		SimpleLifecycle lifecycle = new SimpleLifecycle();

		Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle);

		Query query = Query.builder()
		        .pid(22l)
		        .pid(23l)
		        .build();

		SimpleMockConnection connection = SimpleMockConnection.builder()
		        .commandReply("09 02", "SEARCHING...0140:4902015756571:5A5A5A314B5A412:4D363930333932")
		        .commandReply("0100", "\r4100be3ea813")
		        .commandReply("0200", "4140fed00400\n")
		        .commandReply("0115", "\t4 1 1 5 F F f f>\r")
		        .build();

		workflow.start(connection, query);

		WorkflowFinalizer.finalizeAfter500ms(workflow);

		Assertions.assertThat(lifecycle.isErrorOccurred()).isFalse();

		PidDefinition findBy = workflow.getPidRegistry().findBy(22l);
		Assertions.assertThat(findBy).isNotNull();
		
		double ratePerSec = workflow.getDiagnostics().rate().findBy(RateType.MEAN, findBy)
		        .get().getValue();

		Assertions.assertThat(ratePerSec).isGreaterThan(0);
	}

	@Test
	public void readErrorTest() throws IOException, InterruptedException {
		SimpleLifecycle lifecycle = new SimpleLifecycle();

		Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle);

		Query query = Query.builder()
		        .pid(22l)
		        .pid(23l)
		        .build();

		SimpleMockConnection connection = SimpleMockConnection.builder()
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
		SimpleLifecycle lifecycle = new SimpleLifecycle();

		Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle);

		Query query = Query.builder()
		        .pid(22l)
		        .pid(23l)
		        .build();

		SimpleMockConnection connection = SimpleMockConnection.builder()
		        .commandReply("0100", "4100be3ea813")
		        .commandReply("0200", "4140fed00400")
		        .commandReply("0115", "4115FFff")
		        .simulateErrorInReconnect(true)
		        .simulateWriteError(true) // simulate write error
		        .build();

		workflow.start(connection, query);

		WorkflowFinalizer.finalizeAfter500ms(workflow);

		Assertions.assertThat(lifecycle.isErrorOccurred()).isTrue();
	}
}
