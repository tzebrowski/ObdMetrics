package org.obd.metrics.integration;

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
import org.obd.metrics.DataCollector;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.connection.Connection;
import org.obd.metrics.workflow.Workflow;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PerformanceTest extends IntegrationTestBase {

	@Test
	public void t0() throws IOException, InterruptedException, ExecutionException {
		final Connection connection = openConnection();
		final DataCollector collector = new DataCollector();

		final Workflow workflow = Workflow.mode1().equationEngine("JavaScript").metricsObserver(collector).build();
		final Set<String> filter = new HashSet<>();
		filter.add("05");// Engine coolant temperature
		filter.add("0B");// Intake manifold absolute pressure
		filter.add("0C");// Engine RPM
		filter.add("0F");// Intake air temperature
		filter.add("11");// Throttle position
		filter.add("0D");// Vehicle speed
//		filter.add("0E");// 
//		filter.add("13");//
//		filter.add("15");// 
//		filter.add("1c");// 
//		filter.add("1c");// 
				
		workflow
		.connection(connection)
		.filter(filter)
		.batchEnabled(true)
		.start();

		final Callable<String> end = () -> {
			Thread.sleep(1 * 60000);
			log.info("Ending the process of collecting the data");
			workflow.stop();
			return "end";
		};

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(3);
		newFixedThreadPool.invokeAll(Arrays.asList(end));
		double ratePerSec05 = workflow.getStatistics().getRatePerSec(new ObdCommand("0105"));
		double ratePerSec0C = workflow.getStatistics().getRatePerSec(new ObdCommand("010C"));
	
		log.info("Rate: 0105: {}", ratePerSec05);
		log.info("Rate: 010C: {}", ratePerSec0C);
	
		Assertions.assertThat(ratePerSec05).isGreaterThan(10d);
		Assertions.assertThat(ratePerSec0C).isGreaterThan(10d);
	
		newFixedThreadPool.shutdown();
	}
}
