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
import org.obd.metrics.DataCollector;
import org.obd.metrics.connection.Connection;
import org.obd.metrics.workflow.Workflow;

import lombok.extern.slf4j.Slf4j;

//its not really a test ;)
@Slf4j
public class WorkflowTest extends IntegrationTestBase {

	@Test
	public void t0() throws IOException, InterruptedException, ExecutionException {
		final Connection connection = openConnection();
		final DataCollector collector = new DataCollector();

		final Workflow workflow = Workflow.mode1().equationEngine("JavaScript").metricsObserver(collector).build();
		final Set<String> filter = new HashSet<>();
		filter.add("05");//Engine coolant temperature
		filter.add("0B"); //Intake manifold absolute pressure
		filter.add("0C"); //Engine RPM
		filter.add("0F"); //Intake air temperature
		filter.add("11"); //Throttle position
		filter.add("OD"); //Vehicle speed
		filter.add("OE"); //Timing Advance
         
		workflow.start(connection,filter, true);

		final Callable<String> end = () -> {

			Thread.sleep(5 * 60000);
			log.info("Ending the process of collecting the data");
			workflow.stop();
			return "end";
		};

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(3);
		newFixedThreadPool.invokeAll(Arrays.asList(end));
		
		newFixedThreadPool.shutdown();
	}
}
