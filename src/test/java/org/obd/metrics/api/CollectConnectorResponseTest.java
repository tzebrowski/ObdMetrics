package org.obd.metrics.api;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.connection.MockAdapterConnection;
import org.obd.metrics.transport.Connector;

public class CollectConnectorResponseTest {
	
	@Test
	public void emptyConnectorResponseTest() throws IOException, InterruptedException {
		// Specify lifecycle observer
		SimpleLifecycle lifecycle = new SimpleLifecycle();
		
		RawConnectorReponseSizeChecker emptyResponse = new RawConnectorReponseSizeChecker(0);
		
		// Obtain the Workflow instance for mode 01
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle, emptyResponse, "mode01.json", "alfa.json");

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
		        .collectRawConnectorResponseEnabled(Boolean.FALSE)
		        .cachePolicy(
		        		CachePolicy.builder()
		        		.storeResultCacheOnDisk(Boolean.FALSE)
		        		.resultCacheFilePath("./result_cache.json")
		        		.resultCacheEnabled(Boolean.FALSE).build())
		        .adaptiveTimeoutPolicy(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.FALSE)
		                .checkInterval(5)
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
		WorkflowFinalizer.finalizeAfter(workflow, 800);
		
		Assertions.assertThat(emptyResponse.callCount).isGreaterThan(0);
	}
	
	
	@Test
	public void nonEmptyConnectorResponseTest() throws IOException, InterruptedException {
		// Specify lifecycle observer
		SimpleLifecycle lifecycle = new SimpleLifecycle();
		
		RawConnectorReponseSizeChecker emptyResponse = new RawConnectorReponseSizeChecker(Connector.BUFFER_SIZE);
		
		// Obtain the Workflow instance for mode 01
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle, emptyResponse, "mode01.json", "alfa.json");

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
		        .collectRawConnectorResponseEnabled(Boolean.TRUE)
		        .cachePolicy(
		        		CachePolicy.builder()
		        		.storeResultCacheOnDisk(Boolean.FALSE)
		        		.resultCacheFilePath("./result_cache.json")
		        		.resultCacheEnabled(Boolean.FALSE).build())
		        .adaptiveTimeoutPolicy(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.FALSE)
		                .checkInterval(5)
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
		WorkflowFinalizer.finalizeAfter(workflow, 800);
		
		Assertions.assertThat(emptyResponse.callCount).isGreaterThan(0);
	}
}
