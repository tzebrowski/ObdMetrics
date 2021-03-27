package org.obd.metrics.api;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.Lifecycle;
import org.obd.metrics.pid.PidDefinition;

import lombok.Getter;

public class ConnectorTest {

	static class LifecycleImpl implements Lifecycle {

		@Getter
		boolean recieveErrorNotify = false;

		@Getter
		String message;

		@Override
		public void onError(String message, Throwable e) {
			recieveErrorNotify = true;
			this.message = message;
		}
	}

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

		workflow.start(connection, Adjustements
		        .builder()
		        .filter(filter).build());

		CompletionThread.setup(workflow);

		Assertions.assertThat(lifecycle.isRecieveErrorNotify()).isFalse();

		final PidDefinition findBy = workflow.getPidRegistry().findBy("15");
		double ratePerSec = workflow.getStatisticsRegistry().getRatePerSec(findBy);
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

		workflow.start(connection, Adjustements
		        .builder()
		        .filter(filter).build());

		CompletionThread.setup(workflow);

		Assertions.assertThat(lifecycle.isRecieveErrorNotify()).isTrue();
	}

	@Test
	public void reconnectErrorTest() throws IOException, InterruptedException {
		final LifecycleImpl lifecycle = new LifecycleImpl();

		final Workflow workflow = SimpleWorkflowFactory.getMode01Workflow(lifecycle);

		final Set<Long> filter = new HashSet<>();
		filter.add(22l);
		filter.add(23l);

		MockConnection connection = MockConnection.builder()
		        .commandReply("0100", "4100be3ea813")
		        .commandReply("0200", "4140fed00400")
		        .commandReply("0115", "4115FFff")
		        .simulateErrorInReconnect(true)
		        .simulateWriteError(true)
		        .build();

		workflow.start(connection, Adjustements
		        .builder()
		        .filter(filter).build());

		CompletionThread.setup(workflow);

		Assertions.assertThat(lifecycle.isRecieveErrorNotify()).isTrue();
	}
}
