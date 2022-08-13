package org.obd.metrics.api;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.CacheConfig;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.connection.MockAdapterConnection;
import org.obd.metrics.diagnostic.RateSupplier;
import org.obd.metrics.diagnostic.RateType;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PIDsPriorityTest {

	@Test
	public void overrideTest() throws IOException, InterruptedException {

		// Getting the workflow - mode01
		Workflow workflow = SimpleWorkflowFactory.getWorkflow();
		PidDefinitionRegistry pidRegistry = workflow.getPidRegistry();

		// First group
		pidRegistry.findBy(12l).setPriority(0);
		pidRegistry.findBy(13l).setPriority(0);
		pidRegistry.findBy(16l).setPriority(0);
		pidRegistry.findBy(18l).setPriority(0);
		
		//Second group
		pidRegistry.findBy(7l).setPriority(1);
		pidRegistry.findBy(8l).setPriority(1);
		pidRegistry.findBy(17l).setPriority(1);
		
		//Third group
		pidRegistry.findBy(6l).setPriority(2);
		pidRegistry.findBy(22l).setPriority(2);

		// Specify more than 6 commands, so that we have 2 groups
		Query query = Query.builder()
				.pid(7l)  // Short trims 
				.pid(8l)  // Long trim
				.pid(17l) // MAF
				.pid(22l) // Oxygen sensor
		        .pid(6l)  // Engine coolant temperature
		        .pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        .pid(16l) // Intake air temperature
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .pid(15l) // Timing advance
		        .build();

		// Define PID's we want to query, 2 groups, RPM should be queried separately
		MockAdapterConnection mockConnection = MockAdapterConnection.builder()
		        .requestResponse("0100", "4100be3ea813")
		        .requestResponse("0200", "4140fed00400")
		        .requestResponse("01 0B 0C 0F 11 0D 2", "00C0:410BFF0C00001:0F0011000D00AA") 
		        .requestResponse("01 06 07 10 0E 2", "410680078B0E80") 
		        .requestResponse("01 15 05 1", "41155AFF0500") 
		        .build();
		
//		final AdapterConnection connection = BluetoothConnection.of("AABBCC112233"); 

		// Enable priority commands
		Adjustments optional = Adjustments.builder()
		        .cacheConfig(CacheConfig.builder().resultCacheEnabled(Boolean.FALSE).build())
				.batchEnabled(true)
		        .producerPolicy(
		                ProducerPolicy
		                		.builder()
		                		.pidPriority(0, 0)
		                		.pidPriority(1, 5)
		                		.pidPriority(2, 10)
		                		.priorityQueueEnabled(Boolean.TRUE)
		                        .build())
		        .build();

		// Start background threads, that call the adapter,decode the raw data, and
		// populates OBD metrics
		workflow.start(mockConnection, query, optional);

		PidDefinition p0 = pidRegistry.findBy(12l);
		PidDefinition p1 = pidRegistry.findBy(8l);
		PidDefinition p2 = pidRegistry.findBy(22l);

		WorkflowFinalizer.finalizeAfter(workflow, 3000);

		RateSupplier rateCollector = workflow.getDiagnostics().rate();
		double rate1 = rateCollector.findBy(RateType.MEAN,p1).get().getValue();
		double rate0 = rateCollector.findBy(RateType.MEAN,p0).get().getValue();
		double rate2 = rateCollector.findBy(RateType.MEAN,p2).get().getValue();

		log.info("Priority.1 Pid: {}, rate: {}", p1.getPid(), rate1);
		log.info("Priority.0 Pid: {}, rate: {}", p0.getPid(), rate0);
		log.info("Priority.2 Pid: {}, rate: {}", p2.getPid(), rate2);

		Assertions.assertThat(rate1).isGreaterThan(0);
		Assertions.assertThat(rate0).isGreaterThan(0);
		Assertions.assertThat(rate2).isGreaterThan(0);

		Assertions.assertThat(rate0).isGreaterThan(rate1);
		Assertions.assertThat(rate1).isGreaterThan(rate2);
		
		
	}
}