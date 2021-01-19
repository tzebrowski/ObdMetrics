package org.openobd2.core.integration;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections4.MultiValuedMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.DataCollector;
import org.openobd2.core.command.Command;
import org.openobd2.core.command.CommandReply;
import org.openobd2.core.connection.Connection;
import org.openobd2.core.workflow.Workflow;

import lombok.extern.slf4j.Slf4j;

//its not really a test ;)
@Slf4j
public class WorkflowTest extends IntegrationTestBase {

	@Test
	public void mode1Test() throws IOException, InterruptedException, ExecutionException {
		final Connection connection = openConnection();
		final DataCollector collector = new DataCollector();

		final Workflow workflow = Workflow.mode1().equationEngine("JavaScript").subscriber(collector).build();
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

			Thread.sleep(20000);
			log.info("Ending the process of collecting the data");
			workflow.stop();
			return "end";
		};

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(3);
		newFixedThreadPool.invokeAll(Arrays.asList(end));

		final MultiValuedMap<Command, CommandReply<?>> data = collector.getData();
		Assertions.assertThat(data).isNotNull();

		newFixedThreadPool.shutdown();
	}
}
