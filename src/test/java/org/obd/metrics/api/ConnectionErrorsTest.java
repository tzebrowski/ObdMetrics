package org.obd.metrics.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;
import org.obd.metrics.Lifecycle;
import org.obd.metrics.command.group.Mode1CommandGroup;
import org.obd.metrics.pid.Urls;

import lombok.Getter;

public class ConnectionErrorsTest {

	static class LifecycleImpl implements Lifecycle {

		@Getter
		boolean recieveErrorNotify = false;

		@Getter
		String message;

		public void onError(String message, Throwable e) {
			recieveErrorNotify = true;
			this.message = message;
		}
	}

	@Test
	public void simulateWriteErrorTest() throws IOException, InterruptedException {
		final LifecycleImpl lifecycle = new LifecycleImpl();

		final Workflow workflow = WorkflowFactory.mode1().equationEngine("JavaScript")
		        .lifecycle(lifecycle)
		        .pidSpec(PidSpec
		                .builder()
		                .initSequence(Mode1CommandGroup.INIT_NO_DELAY)
		                .pidFile(Urls.resourceToUrl("mode01.json")).build())
		        .observer(new DataCollector())
		        .initialize();

		final Set<Long> filter = new HashSet<>();
		filter.add(22l);
		filter.add(23l);

		MockConnection connection = MockConnection.builder()
		        .commandReply("0100", "4100be3ea813")
		        .commandReply("0200", "4140fed00400")
		        .commandReply("0115", "4115FFff")
		        .simulateWriteError(true)
		        .build();

		workflow.start(WorkflowContext
		        .builder()
		        .connection(connection)
		        .filter(filter).build());

		final Callable<String> end = () -> {
			Thread.sleep(500);
			workflow.stop();
			return "end";
		};

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(3);
		newFixedThreadPool.invokeAll(Arrays.asList(end));
		newFixedThreadPool.shutdown();

		Assertions.assertThat(lifecycle.isRecieveErrorNotify()).isTrue();
	}
}
