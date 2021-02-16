package org.obd.metrics.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DummyObserver;
import org.obd.metrics.StatusObserver;
import org.obd.metrics.command.group.Mode1CommandGroup;

import lombok.Getter;

public class ConnectionIssueTest {

	static class Status implements StatusObserver {

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
	public void recieveErrorNotifyTest() throws IOException, InterruptedException, ExecutionException {
		final Status status = new Status();
		
		final Workflow workflow = Workflow.mode1().equationEngine("JavaScript")
				.statusObserver(status)
				.ecuSpecific(EcuSpecific
						.builder()
						.initSequence(Mode1CommandGroup.INIT_NO_DELAY)
						.pidFile("mode01.json").build())
				.observer(new DummyObserver()).build();

		final Set<Long> filter = new HashSet<>();
		filter.add(22l);//
		filter.add(23l);//
		
		MockConnection connection = MockConnection.builder().
				commandReply("0100", "4100be3ea813").
				commandReply("0200", "4140fed00400").
				commandReply("0115", "4115FFff")
				.simulateWriteError(true)
				.build();
				
		workflow.connection(connection).filter(filter).batch(false).start();

		final Callable<String> end = () -> {
			Thread.sleep(1500);
			workflow.stop();
			return "end";
		};

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(3);
		newFixedThreadPool.invokeAll(Arrays.asList(end));
		newFixedThreadPool.shutdown();

		Assertions.assertThat(status.isRecieveErrorNotify()).isTrue();
	}
	
	
	
	@Test
	public void closedConnectionTest() throws IOException, InterruptedException, ExecutionException {
		final Status status = new Status();
		
		final Workflow workflow = Workflow.mode1().equationEngine("JavaScript")
				.statusObserver(status)
				.ecuSpecific(EcuSpecific
						.builder()
						.initSequence(Mode1CommandGroup.INIT_NO_DELAY)
						.pidFile("mode01.json").build())
				.observer(new DummyObserver()).build();

		final Set<Long> filter = new HashSet<>();
		filter.add(22l);//
		filter.add(23l);//
		
		MockConnection connection = MockConnection.builder().
				commandReply("0100", "4100be3ea813").
				commandReply("0200", "4140fed00400").
				commandReply("0115", "4115FFff")
				.closedConnnection(true)
				.build();
				
		workflow.connection(connection).filter(filter).batch(false).start();

		final Callable<String> end = () -> {
			Thread.sleep(1500);
			workflow.stop();
			return "end";
		};

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(3);
		newFixedThreadPool.invokeAll(Arrays.asList(end));
		newFixedThreadPool.shutdown();

		Assertions.assertThat(status.isRecieveErrorNotify()).isTrue();
		Assertions.assertThat(status.getMessage()).isEqualTo("Device connection is faulty. Finishing communication.");
	}
	
}
