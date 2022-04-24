package org.obd.metrics.api;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.connection.SimpleMockConnection;
import org.obd.metrics.diagnostic.RateSupplier;
import org.obd.metrics.diagnostic.RateType;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GroupsTest {

	@Test
	public void t0() throws IOException, InterruptedException {

		// Getting the workflow - mode01
		Workflow workflow = SimpleWorkflowFactory.getWorkflow();
		PidDefinitionRegistry pidRegistry = workflow.getPidRegistry();

		// First group
		pidRegistry.findBy(12l).setPriority(0);
		pidRegistry.findBy(13l).setPriority(0);
		pidRegistry.findBy(16l).setPriority(0);
		pidRegistry.findBy(18l).setPriority(0);
		pidRegistry.findBy(12l).setPriority(0);
		pidRegistry.findBy(14l).setPriority(0);
		pidRegistry.findBy(15l).setPriority(0);

		pidRegistry.findBy(7l).setPriority(1);
		pidRegistry.findBy(8l).setPriority(1);
		pidRegistry.findBy(17l).setPriority(1);
		pidRegistry.findBy(22l).setPriority(1);

		//Second group
		pidRegistry.findBy(6l).setPriority(2);

		// Specify more than 6 commands, so that we have 2 groups
		Query query = Query.builder()
				.pid(7l) // Short trims 
				.pid(8l)  // Long trim
				.pid(17l) // MAF
				.pid(22l) // Oxygen sensor
		        
		        .pid(6l) // Engine coolant temperature
		        .pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        .pid(16l) // Intake air temperature
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .pid(15l) // Timing advance
		        .build();

		// Define PID's we want to query, 2 groups, RPM should be queried separately
		SimpleMockConnection connection = SimpleMockConnection.builder()
		        .commandReply("0100", "4100be3ea813")
		        .commandReply("0200", "4140fed00400")
		        .commandReply("01 05", "410500") 
		        .commandReply("01 0B 0C 0F 11 0D 0E", "00e0:410bff0c00001:of0011000d800f2:00aaaaaaaaaaaa") // group 2,
		                                                                                                   // fast group
		        .build();

		// Enable priority commands
		Adjustments optional = Adjustments.builder()
		        .batchEnabled(true)
		        .producerPolicy(
		                ProducerPolicy.builder()
		                        .priorityQueueEnabled(Boolean.TRUE)
		                        .lowPriorityCommandFrequencyDelay(300)
		                        .build())
		        .build();

		// Start background threads, that call the adapter,decode the raw data, and
		// populates OBD metrics
		workflow.start(connection, query, optional);

		PidDefinition p1 = pidRegistry.findBy(6l);// Engine coolant temperature
		PidDefinition p2 = pidRegistry.findBy(13l);// Engine RPM

		WorkflowFinalizer.finalizeAfter(workflow, 1500);

		RateSupplier rateCollector = workflow.getDiagnostics().rate();
		double rate1 = rateCollector.findBy(RateType.MEAN,p1).get().getValue();
		double rate2 = rateCollector.findBy(RateType.MEAN,p2).get().getValue();

		log.info("Pid: {}, rate: {}", p1.getDescription(), rate1);
		log.info("Pid: {}, rate: {}", p2.getDescription(), rate2);

		Assertions.assertThat(rate1).isGreaterThan(0);
		Assertions.assertThat(rate2).isGreaterThan(0);

		// Engine coolant temperatur should have less RPS than RPM
		Assertions.assertThat(rate1).isLessThanOrEqualTo(rate2);
	}
}
