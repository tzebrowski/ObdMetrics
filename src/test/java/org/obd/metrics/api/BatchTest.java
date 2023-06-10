package org.obd.metrics.api;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.connection.MockAdapterConnection;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;

public class BatchTest {
	
	@Test
	public void mode22() throws IOException, InterruptedException {

		// Create an instance of DataCollector that receives the OBD Metrics
		DataCollector collector = new DataCollector();

		// Getting the Workflow instance for mode 01
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(collector);

		// Query for specified PID's like: Engine coolant temperature
		Query query = Query.builder()
		        .pid(6015l)  // Oil temp
		        .pid(6008l)  // Coolant
		        .pid(6007l) // IAT
		        .build();

		// Create an instance of mock connection with additional commands and replies
		MockAdapterConnection connection = MockAdapterConnection.builder()
		        .requestResponse("22 194F 1003 1935 2", "00B0:62194F2E65101:0348193548").build();

		// Enabling batch commands
		final Adjustments optional = Adjustments
		        .builder()
		        .cacheConfig(
		        		CachePolicy.builder()
		        		.storeResultCacheOnDisk(Boolean.FALSE)
		        		.resultCacheFilePath("./result_cache.json")
		        		.resultCacheEnabled(Boolean.TRUE).build())
		        .adaptiveTiming(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.FALSE)
		                .checkInterval(5)
		                .commandFrequency(6)
		                .build())
		        .producerPolicy(ProducerPolicy.builder()
		                .priorityQueueEnabled(Boolean.TRUE)
		                .build())
		        .batchEnabled(Boolean.TRUE)
		        .build();

		// Start background threads, that call the adapter,decode the raw data, and
		// populates OBD metrics
		workflow.start(connection, query, optional);

		// Starting the workflow completion job, it will end workflow after some period
		// of time (helper method)
		WorkflowFinalizer.finalizeAfter(workflow,1000);

		// Ensure batch commands were sent out
		Assertions.assertThat(connection.recordedQueries())
			.contains("22 194F 1003 1935 2");

		// Ensure we receive AT commands
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();

		PidDefinition coolant = workflow.getPidRegistry().findBy(6007l);

		// Ensure we receive Coolant temperature metric
		ObdMetric metric = collector.findSingleMetricBy(coolant);
		Assertions.assertThat(metric).isNotNull();

		Assertions.assertThat(metric.getValue()).isInstanceOf(Double.class);
		Assertions.assertThat(metric.getValue()).isEqualTo(5.0);
	}
	
	
	@Test
	public void moreThan6PriorityCommands() throws IOException, InterruptedException {

		// Create an instance of DataCollector that receives the OBD Metrics
		DataCollector collector = new DataCollector();

		// Getting the Workflow instance for mode 01
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(collector);

		PidDefinitionRegistry pidRegistry = workflow.getPidRegistry();
		pidRegistry.findBy(7l).setPriority(0);
		pidRegistry.findBy(8l).setPriority(0);
		pidRegistry.findBy(17l).setPriority(0);
		pidRegistry.findBy(22l).setPriority(0);
		pidRegistry.findBy(6l).setPriority(0);
		pidRegistry.findBy(12l).setPriority(0);
		pidRegistry.findBy(13l).setPriority(0);
		pidRegistry.findBy(16l).setPriority(0);

		pidRegistry.findBy(18l).setPriority(1);
		pidRegistry.findBy(14l).setPriority(1);
		pidRegistry.findBy(15l).setPriority(1);

		
		// Query for specified PID's like: Engine coolant temperature
		Query query = Query.builder()
				.pid(7l) // Short trims 
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
		        .pid(9000l) // Battery voltage
		        .build();

		// Create an instance of mock connection with additional commands and replies
		// It contains 2 priority 0 groups
		MockAdapterConnection connection = MockAdapterConnection.builder()
		        .requestResponse("0100", "4100be3ea813")
		        .requestResponse("0200", "4140fed00400")
		        .requestResponse("01 06 07 10 15 05 0B 3", "00C0:410680078B151:5AFF05000BFFAA") //
		        .requestResponse("01 0C 0F 1", "410c00000f00")
		        .build();
		
		// Enabling batch commands
		final Adjustments optional = Adjustments
		        .builder()
		        .cacheConfig(
		        		CachePolicy.builder()
		        		.storeResultCacheOnDisk(Boolean.FALSE)
		        		.resultCacheFilePath("./result_cache.json")
		        		.resultCacheEnabled(Boolean.TRUE).build())
		        .adaptiveTiming(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.FALSE)
		                .checkInterval(5000)
		                .commandFrequency(6)
		                .build())
		        .producerPolicy(ProducerPolicy.builder()
		                .priorityQueueEnabled(Boolean.TRUE)
		                .build())
		        .batchEnabled(Boolean.TRUE)
		        .build();

		// Start background threads, that call the adapter,decode the raw data, and
		// populates OBD metrics
		workflow.start(connection, query, optional);

		// Starting the workflow completion job, it will end workflow after some period
		// of time (helper method)
		WorkflowFinalizer.finalizeAfter(workflow,1500);
		
//		// Ensure batch commands were sent out
		Assertions.assertThat(connection.recordedQueries())
			.contains("01 06 07 10 15 05 0B 3")
			.contains("01 0C 0F 1");
		
		// Ensure we receive AT commands
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();

		PidDefinition coolant = pidRegistry.findBy(6l);

		// Ensure we receive coolant temperature metric
		ObdMetric metric = collector.findSingleMetricBy(coolant);
		Assertions.assertThat(metric).isNotNull();

		Assertions.assertThat(metric.getValue()).isInstanceOf(Integer.class);
		Assertions.assertThat(metric.getValue()).isEqualTo(-40);
	}
	
	@Test
	public void batchTest() throws IOException, InterruptedException {

		// Create an instance of DataCollector that receives the OBD Metrics
		DataCollector collector = new DataCollector();

		// Getting the Workflow instance for mode 01
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(collector);

		// Query for specified PID's like: Engine coolant temperature
		Query query = Query.builder()
		        .pid(6l) // Engine coolant temperature
		        .pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        .pid(16l) // Intake air temperature
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .build();

	
		// Create an instance of mock connection with additional commands and replies
		MockAdapterConnection connection = MockAdapterConnection.builder()
		        .requestResponse("0100", "4100BE3EA813")
		        .requestResponse("0200", "4140FED00400")
		        .requestResponse("01 0B 0C 11 0D 05 0F 3", "00E0:410BFF0C00001:11000D0005000F2:00AAAAAAAAAAAA").build();

		// Enabling batch commands
		Adjustments optional = Adjustments
		        .builder()
		        .batchEnabled(true)
		        .build();

		// Start background threads, that call the adapter,decode the raw data, and
		// populates OBD metrics
		workflow.start(connection, query, optional);

		// Starting the workflow completion job, it will end workflow after some period
		// of time (helper method)
		WorkflowFinalizer.finalizeAfter(workflow,1500L);

		// Ensure batch commands were sent out
		Assertions.assertThat(connection.recordedQueries())
			.contains("01 0B 0C 11 0D 05 0F 3");

		// Ensure we receive AT commands
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();

		PidDefinition coolant = workflow.getPidRegistry().findBy(6l);

		// Ensure we receive Coolant temperature metric
		ObdMetric metric = collector.findSingleMetricBy(coolant);
		Assertions.assertThat(metric).isNotNull();

		Assertions.assertThat(metric.getValue()).isInstanceOf(Integer.class);
		Assertions.assertThat(metric.getValue()).isEqualTo(-40);
	}

	@Test
	public void batchLessThan6Test() throws IOException, InterruptedException {

		DataCollector collector = new DataCollector();
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(collector);

		Query query = Query.builder()
		        .pid(6l) // Engine coolant temperature
		        .pid(12l)// Intake manifold absolute pressure
		        .build();

		MockAdapterConnection connection = MockAdapterConnection.builder()
		        .requestResponse("0100", "4100BE3EA813")
		        .requestResponse("0200", "4140FED00400")
		        .requestResponse("01 0B 05 1", "410Bff0500").build();

		Adjustments optional = Adjustments
		        .builder()
		        .batchEnabled(true).build();

		workflow.start(connection, query, optional);

		WorkflowFinalizer.finalizeAfter(workflow,700);

		// Ensure batch commands were sent out
		Assertions.assertThat(connection.recordedQueries())
			.contains("01 0B 05 1");

		// Ensure we receive AT command as well
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();

		PidDefinition coolant = workflow.getPidRegistry().findBy(6l);

		// Ensure we receive Coolant temperature metric
		ObdMetric metric = collector.findSingleMetricBy(coolant);
		Assertions.assertThat(metric).isNotNull();

		Assertions.assertThat(metric.getValue()).isInstanceOf(Integer.class);
		Assertions.assertThat(metric.getValue()).isEqualTo(-40);
	
	}

	@Test
	public void nonBatchTest() throws IOException, InterruptedException {

		DataCollector collector = new DataCollector();
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(collector);

		Query query = Query.builder()
		        .pid(6l) // Engine coolant temperature
		        .pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        .pid(16l) // Intake air temperature
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .build();

		MockAdapterConnection connection = MockAdapterConnection.builder()
		        .requestResponse("0100", "4100be3ea813")
		        .requestResponse("0200", "4140fed00400")
		        .requestResponse("0105", "410522")
		        .requestResponse("010C", "410c541B")
		        .requestResponse("010D", "")
		        .requestResponse("0111", "no data")
		        .requestResponse("010B", "410b35")
		        .readTimeout(0)
		        .build();

		workflow.start(connection, query);

		WorkflowFinalizer.finalizeAfter500ms(workflow);
		
		// Ensure batch commands were sent out
		Assertions.assertThat(connection.recordedQueries())
			.contains("0105");

		// Ensure we receive AT command as well
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();

		PidDefinition coolant = workflow.getPidRegistry().findBy(6l);

		// Ensure we receive Coolant temperature metric
		ObdMetric metric = collector.findSingleMetricBy(coolant);
		Assertions.assertThat(metric).isNotNull();

		Assertions.assertThat(metric.getValue()).isInstanceOf(Integer.class);
		Assertions.assertThat(metric.getValue()).isEqualTo(-6);
		Assertions.assertThat(metric.valueToDouble()).isEqualTo(-6.0);
		Assertions.assertThat(metric.valueToString()).isEqualTo("-6");
	}
}
