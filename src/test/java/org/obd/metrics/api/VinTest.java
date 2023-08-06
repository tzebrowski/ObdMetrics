package org.obd.metrics.api;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.connection.MockAdapterConnection;

public class VinTest {

	@Test
	public void correctTest() throws IOException, InterruptedException {
		// Specify lifecycle observer
		SimpleLifecycle lifecycle = new SimpleLifecycle();

		// Specify the metrics collector
		DataCollector collector = new DataCollector();

		// Obtain the Workflow instance for mode 01
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle, collector);

		// Define PID's we want to query
		Query query = Query.builder()
		        .pid(6l) // Engine coolant temperature
		        .pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        .pid(16l) // Intake air temperature
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .build();

		// Define mock connection with VIN data "09 02" command
		MockAdapterConnection connection = MockAdapterConnection.builder()
		        .requestResponse("0902", "SEARCHING...0140:4902015756571:5A5A5A314B5A412:4D363930333932")
		        .requestResponse("0100", "4100be3ea813")
		        .requestResponse("0200", "4140fed00400")
		        .requestResponse("0105", "410522")
		        .requestResponse("010C", "410c541B")
		        .requestResponse("010B", "410b35")
		        .build();

		
		Adjustments optional = Adjustments
		        .builder()
		        .vehicleMetadataReadingEnabled(Boolean.TRUE)
		        .cachePolicy(
		        		CachePolicy.builder()
		        		.storeResultCacheOnDisk(Boolean.FALSE)
		        		.resultCacheEnabled(Boolean.FALSE).build())
		        .adaptiveTimeoutPolicy(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.FALSE)
		                .commandFrequency(6)
		                .build())
		        .producerPolicy(ProducerPolicy.builder()
		                .priorityQueueEnabled(Boolean.TRUE)
		                .build())
		        .batchPolicy(BatchPolicy.builder().enabled(Boolean.TRUE).build())
		        .build();
		
		// Start background threads, that call the adapter,decode the raw data, and
		// populates OBD metrics
		workflow.start(connection, query, optional);

		// Starting the workflow completion job, it will end workflow after some period
		// of time (helper method)
		WorkflowFinalizer.finalize(workflow);

		// Ensure we receive AT command
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();

		// Ensure Device Properties Holder contains VIN
		// 0140:4902015756571:5A5A5A314B5A412:4D363930333932 -> WVWZZZ1KZAM690392
		Assertions.assertThat(lifecycle.getMetadata()).containsEntry("VIN", "WVWZZZ1KZAM690392");
	}

	@Test
	public void incorrectTest() throws IOException, InterruptedException {
		SimpleLifecycle lifecycle = new SimpleLifecycle();

		// Specify metrics collector
		DataCollector collector = new DataCollector();
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle, collector);

		Query query = Query.builder()
		        .pid(6l) // Engine coolant temperature
		        .pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        .pid(16l) // Intake air temperature
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .build();

		String vinMessage = "0140:4802015756571:5A5A5A314B5A412:4D363930333932";

		// Define mock connection with VIN data "09 02" command
		MockAdapterConnection connection = MockAdapterConnection.builder()
		        .requestResponse("0902", vinMessage)
		        .requestResponse("0100", "4100BE3EA813")
		        .requestResponse("0200", "4140FED00400")
		        .requestResponse("0105", "410522")
		        .requestResponse("010C", "410C541B")
		        .requestResponse("010B", "410B35")
		        .build();
		
		
		Adjustments optional = Adjustments
	        .builder()
	        .vehicleMetadataReadingEnabled(Boolean.TRUE)
	        .cachePolicy(
	        		CachePolicy.builder()
	        		.storeResultCacheOnDisk(Boolean.FALSE)
	        		.resultCacheEnabled(Boolean.FALSE).build())
	        .adaptiveTimeoutPolicy(AdaptiveTimeoutPolicy
	                .builder()
	                .enabled(Boolean.FALSE)
	                .commandFrequency(6)
	                .build())
	        .producerPolicy(ProducerPolicy.builder()
	                .priorityQueueEnabled(Boolean.TRUE)
	                .build())
	        .batchPolicy(BatchPolicy.builder().enabled(Boolean.TRUE).build())
	        .build();
	
		
		// Start background threads, that call the adapter,decode the raw data, and
		// populates OBD metrics
		workflow.start(connection, query, optional);

		// Starting the workflow completion job, it will end workflow after some period
		// of time (helper method)
		WorkflowFinalizer.finalize(workflow);

		// Ensure we receive AT command
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();

		// failed decoding VIN
		Assertions.assertThat(lifecycle.getMetadata()).containsEntry("VIN", vinMessage);
	}
}
