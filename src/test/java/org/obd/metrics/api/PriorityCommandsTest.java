package org.obd.metrics.api;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.WorkflowFinalizer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PriorityCommandsTest {

	@Test
	public void t0() throws IOException, InterruptedException {

		// Getting the workflow - mode01
		var workflow = SimpleWorkflowFactory.getMode01Workflow();

		// Specify more than 6 commands, so that we have 2 groups
		var query = Query.builder()
		        .pid(6l) // Engine coolant temperature
		        .pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        .pid(16l) // Intake air temperature
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .pid(15l) // Timing advance
		        .build();

		// Define PID's we want to query, 2 groups, RPM should be queried separately
		var connection = MockConnection.builder()
		        .commandReply("0100", "4100be3ea813")
		        .commandReply("0200", "4140fed00400")
		        .commandReply("01 05", "410500") // group 1, slower one
		        .commandReply("01 0B 0C 11 0D 0E 0F", "00e0:410bff0c00001:11000d000e800f2:00aaaaaaaaaaaa") // group 2,
		                                                                                                   // fast group
		        .build();

		// Enable priority commands
		var optional = Adjustments.builder()
		        .initDelay(0)
		        .batchEnabled(true)
		        .producerPolicy(
		                ProducerPolicy.builder()
		                        .priorityQueueEnabled(Boolean.TRUE)
		                        .lowPriorityCommandFrequencyDelay(100)
		                        .build())
		        .build();

		// Start background threads, that call the adapter,decode the raw data, and
		// populates OBD metrics
		workflow.start(connection, query, optional);

		var p1 = workflow.getPidRegistry().findBy(6l);// Engine coolant temperature
		var p2 = workflow.getPidRegistry().findBy(13l);// Engine RPM
		var statisticsRegistry = workflow.getStatisticsRegistry();

		WorkflowFinalizer.finalizeAfter(workflow, 1000,
		        () -> statisticsRegistry.getRatePerSec(p1) > 0 && statisticsRegistry.getRatePerSec(p2) > 0);

		var rate1 = statisticsRegistry.getRatePerSec(p1);
		var rate2 = statisticsRegistry.getRatePerSec(p2);

		log.info("Pid: {}, rate: {}", p1.getDescription(), rate1);
		log.info("Pid: {}, rate: {}", p2.getDescription(), rate2);

		Assertions.assertThat(rate1).isGreaterThan(0);
		Assertions.assertThat(rate2).isGreaterThan(0);

		// Engine coolant temperatur should have less RPS than RPM
		Assertions.assertThat(rate1).isLessThanOrEqualTo(rate2);
	}
}
