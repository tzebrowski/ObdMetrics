package org.obd.metrics.integration;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.obd.metrics.Reply;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.connection.Connection;
import org.obd.metrics.workflow.Workflow;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MultipleDecodersTest extends IntegrationTestBase {

	@Test
	public void t0() throws IOException, InterruptedException, ExecutionException {
		final Connection connection = openConnection();

		final Workflow workflow = Workflow.mode1().equationEngine("JavaScript").observer(new ReplyObserver() {
			@Override
			public void onNext(Reply metric) {
				log.info("Receive data: {}", metric);
			}
		}).build();

		final Set<Long> filter = new HashSet<>();
		filter.add(23l);//
		filter.add(22l);//

		workflow.connection(connection).filter(filter).batchEnabled(true).start();

		final Callable<String> end = () -> {
			Thread.sleep(15000);
			log.info("Ending the process of collecting the data");
			workflow.stop();
			return "end";
		};

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(3);
		newFixedThreadPool.invokeAll(Arrays.asList(end));
		newFixedThreadPool.shutdown();
	}
}
