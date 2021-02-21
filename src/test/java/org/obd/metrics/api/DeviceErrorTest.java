package org.obd.metrics.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;
import org.obd.metrics.DeviceProperties;
import org.obd.metrics.StatusObserver;
import org.obd.metrics.command.group.Mode1CommandGroup;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeviceErrorTest {

	static class Notifications implements StatusObserver {

		@Getter
		boolean errorOccurred = false;

		@Getter
		String message;

		public void onError(String message, Throwable e) {
			errorOccurred = true;
			this.message = message;
		}
		
		@Override
		public void onConnected(DeviceProperties info) {
			log.info("Device properties {}",info.getProperties());
		}
		
		void reset(){
			message = null;
			errorOccurred = false;
		}
	}

	@Test
	public void errorsTest() throws IOException, InterruptedException {
		final Notifications notifications = new Notifications();

		final Workflow workflow = WorkflowFactory
				.mode1()
				.equationEngine("JavaScript")
				.statusObserver(notifications).
				ecuSpecific(EcuSpecific.builder().initSequence(Mode1CommandGroup.INIT_NO_DELAY).pidFile("mode01.json").build())
				.observer(new DataCollector())
				.commandFrequency(0l)
				.initialize();

		final Set<Entry<String, String>> errors = Map.of("can Error","canerror",
														"bus init","businit",
														"STOPPED","stopped",
														"ERROR","error",
														"Unable To Connect","unabletoconnect").entrySet();

		for (final Map.Entry<String,String> input : errors) {
			notifications.reset();
			
			final Set<Long> filter = new HashSet<>();
			filter.add(22l);
			filter.add(23l);

			MockConnection connection = MockConnection
					.builder()
					.commandReply("ATRV","12v")
					.commandReply("0100", "4100be3ea813")
					.commandReply("0200", "4140fed00400")
					.commandReply("0115",input.getKey())
					.build();

			workflow.start(WorkflowContext
					.builder()
					.connection(connection)
					.filter(filter).build());

			final Callable<String> end = () -> {
				Thread.sleep(200);
				workflow.stop();
				return "end";
			};

			final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(3);
			newFixedThreadPool.invokeAll(Arrays.asList(end));
			newFixedThreadPool.shutdown();

			Assertions.assertThat(notifications.isErrorOccurred()).isTrue();
			Assertions.assertThat(notifications.getMessage()).isEqualTo(input.getValue());
		}
	}
}