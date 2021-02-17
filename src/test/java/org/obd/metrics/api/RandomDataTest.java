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
import org.obd.metrics.DummyObserver;
import org.obd.metrics.command.group.AlfaMed17CommandGroup;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RandomDataTest {
	
	@Test
	public void randomTest() throws IOException, InterruptedException  {
	
		final Workflow workflow = WorkflowFactory.generic()
				.equationEngine("JavaScript")
				.ecuSpecific(EcuSpecific
					.builder()
					.initSequence(AlfaMed17CommandGroup.CAN_INIT_NO_DELAY)
					.pidFile("alfa.json").build())
				.observer(new DummyObserver())
				.build();
		
		final Set<Long> ids = new HashSet<>();
		ids.add(8l); // Coolant
		ids.add(4l); // RPM
		ids.add(7l); // Intake temp
		ids.add(15l);// Oil temp
		ids.add(3l); // Spark Advance
		
		final MockConnection connection = MockConnection.builder()
						.commandReply("221003", "xxxxxxxxxxxxxx")
						.commandReply("221000", "")
						.commandReply("221935", "nodata")
						.commandReply("22194f", "stopped")
						.commandReply("221812", "unabletoconnect")
						.build();
		
		
		workflow.connection(connection).filter(ids).batch(false).start();
		final Callable<String> end = () -> {
			Thread.sleep(1 * 1500);
			log.info("Ending the process of collecting the data");
			workflow.stop();
			return "end";
		};

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(1);
		newFixedThreadPool.invokeAll(Arrays.asList(end));
		
		final PidRegistry pids = workflow.getPids();

		PidDefinition pid8l = pids.findBy(8l);
		final double ratePerSec1003 = workflow.getStatistics().getRatePerSec(pid8l);
		log.info("Rate: 1003: {}/sec", ratePerSec1003);
		Assertions.assertThat(ratePerSec1003).isGreaterThan(0);
		
		newFixedThreadPool.shutdown();
	}
}