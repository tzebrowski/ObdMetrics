package org.obd.metrics.api;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.CompletionThread;

public class ConnectorTest {

	@Test
	public void characterTest() throws IOException, InterruptedException {
		final LifecycleImpl lifecycle = new LifecycleImpl();

		final Workflow workflow = SimpleWorkflowFactory.getMode01Workflow(lifecycle);

		final Set<Long> filter = new HashSet<>();
		filter.add(22l);
		filter.add(23l);

		MockConnection connection = MockConnection.builder()
		        .commandReply("09 02", "SEARCHING...0140:4902015756571:5A5A5A314B5A412:4D363930333932")
		        .commandReply("0100", "\r4100be3ea813")
		        .commandReply("0200", "4140fed00400\n")
		        .commandReply("0115", "\t4 1 1 5 F F f f>\r")
		        .build();

		workflow.start(connection,Query.builder().pids(filter).build());

		CompletionThread.setup(workflow);

		Assertions.assertThat(lifecycle.isErrorOccurred()).isFalse();

		double ratePerSec = workflow.getStatisticsRegistry().getRatePerSec(workflow.getPidRegistry().findBy("15"));
		Assertions.assertThat(ratePerSec).isGreaterThan(0);
	}

	@Test
	public void readErrorTest() throws IOException, InterruptedException {
		final LifecycleImpl lifecycle = new LifecycleImpl();

		final Workflow workflow = SimpleWorkflowFactory.getMode01Workflow(lifecycle);

		final Set<Long> filter = new HashSet<>();
		filter.add(22l);
		filter.add(23l);

		MockConnection connection = MockConnection.builder()
		        .commandReply("0100", "4100be3ea813")
		        .commandReply("0200", "4140fed00400")
		        .commandReply("0115", "4115FFff")
		        .simulateReadError(true)
		        .build();

		workflow.start(connection,Query.builder().pids(filter).build());

		CompletionThread.setup(workflow);

		Assertions.assertThat(lifecycle.isErrorOccurred()).isTrue();
	}

	@Test
	public void reconnectErrorTest() throws IOException, InterruptedException {
		final LifecycleImpl lifecycle = new LifecycleImpl();

		final Workflow workflow = SimpleWorkflowFactory.getMode01Workflow(lifecycle);

		final Set<Long> ids = new HashSet<>();
		ids.add(22l);
		ids.add(23l);

		MockConnection connection = MockConnection.builder()
		        .commandReply("0100", "4100be3ea813")
		        .commandReply("0200", "4140fed00400")
		        .commandReply("0115", "4115FFff")
		        .simulateErrorInReconnect(true)
		        .simulateWriteError(true)
		        .build();

		workflow.start(connection,Query.builder().pids(ids).build());

		CompletionThread.setup(workflow);

		Assertions.assertThat(lifecycle.isErrorOccurred()).isTrue();
	}
}
