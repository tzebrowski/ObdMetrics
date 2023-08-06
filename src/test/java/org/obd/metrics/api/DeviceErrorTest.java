package org.obd.metrics.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.connection.MockAdapterConnection;
import org.obd.metrics.executor.CommandExecutionStatus;

public class DeviceErrorTest {

	@SuppressWarnings("serial")
	@Test
	public void errorsTest() throws IOException, InterruptedException {
		SimpleLifecycle lifecycle = new SimpleLifecycle();

		Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle);

		Map<String, String> errors = new HashMap<String, String>() {
			{
				put("can Error", "CANERROR");
				put("bus init", "BUSINIT");
				put("STOPPED", "STOPPED");
				put("ERROR", "ERROR");
				put("Unable To Connect", "UNABLETOCONNECT");
			}
		};

		for (final Entry<String, String> input : errors.entrySet()) {
			lifecycle.reset();

			Query query = Query.builder()
			        .pid(22l)
			        .pid(23l)
			        .build();

			MockAdapterConnection connection = MockAdapterConnection
			        .builder()
			        .requestResponse("ATRV", "12v")
			        .requestResponse("0100", "4100BE3EA813")
			        .requestResponse("0200", "4140FED00400")
			        .requestResponse("0115", input.getKey())
			        .build();

			workflow.start(connection, query);

			WorkflowFinalizer.finalize(workflow);

			Assertions.assertThat(lifecycle.isErrorOccurred()).isTrue();
			Assertions.assertThat(lifecycle.getMessage()).isEqualTo(input.getValue());
		}
	}
	
	@Test
	public void timeoutTest() throws IOException, InterruptedException {
		SimpleLifecycle lifecycle = new SimpleLifecycle();
		
		Adjustments optional = Adjustments
		        .builder()
		        .vehicleMetadataReadingEnabled(Boolean.FALSE)
		        .vehicleCapabilitiesReadingEnabled(Boolean.FALSE)
		        .cacheConfig(
		        		CachePolicy.builder()
		        		.storeResultCacheOnDisk(Boolean.FALSE)
		        		.resultCacheEnabled(Boolean.FALSE).build())
		        .adaptiveTiming(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.FALSE)
		                .commandFrequency(6)
		                .build())
		        .producerPolicy(ProducerPolicy.builder()
		                .priorityQueueEnabled(Boolean.TRUE)
		                .build())
		        .batchPolicy(BatchPolicy.builder().enabled(Boolean.TRUE).build())
		        .build();
		
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle);

		@SuppressWarnings("serial")
		Map<String, String> errors = new HashMap<String, String>() {
			{
				put("FCRXTIMEOUT", CommandExecutionStatus.ERR_TIMEOUT.getMessage());
			}
		};

		for (final Entry<String, String> input : errors.entrySet()) {
			lifecycle.reset();

			Query query = Query.builder()
			        .pid(22l)
			        .pid(23l)
			        .build();

			MockAdapterConnection connection = MockAdapterConnection
			        .builder()
			        .requestResponse("ATRV", "12v")
			        .requestResponse("0100", "4100BE3EA813")
			        .requestResponse("0200", "4140FED00400")
			        .requestResponse("01 15 1", input.getKey())
			        .build();

			workflow.start(connection, query,optional);

			WorkflowFinalizer.finalizeAfter(workflow,1000);

			Assertions.assertThat(lifecycle.isErrorOccurred()).isTrue();
			Assertions.assertThat(lifecycle.getMessage()).isEqualTo(input.getValue());
		}
	}
	
	
	@Test
	public void lvresetTest() throws IOException, InterruptedException {
		SimpleLifecycle lifecycle = new SimpleLifecycle();
		
		Adjustments optional = Adjustments
		        .builder()
		        .vehicleMetadataReadingEnabled(Boolean.FALSE)
		        .vehicleCapabilitiesReadingEnabled(Boolean.FALSE)
		        .cacheConfig(
		        		CachePolicy.builder()
		        		.storeResultCacheOnDisk(Boolean.FALSE)
		        		.resultCacheEnabled(Boolean.FALSE).build())
		        .adaptiveTiming(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.FALSE)
		                .commandFrequency(6)
		                .build())
		        .producerPolicy(ProducerPolicy.builder()
		                .priorityQueueEnabled(Boolean.TRUE)
		                .build())
		        .batchPolicy(BatchPolicy.builder().enabled(Boolean.TRUE).build())
		        .build();
		
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle);

		@SuppressWarnings("serial")
		Map<String, String> errors = new HashMap<String, String>() {
			{
				put("LVRESET", CommandExecutionStatus.ERR_LVRESET.getMessage());
			}
		};

		for (final Entry<String, String> input : errors.entrySet()) {
			lifecycle.reset();

			Query query = Query.builder()
			        .pid(22l)
			        .pid(23l)
			        .build();

			MockAdapterConnection connection = MockAdapterConnection
			        .builder()
			        .requestResponse("ATRV", "12v")
			        .requestResponse("0100", "4100BE3EA813")
			        .requestResponse("0200", "4140FED00400")
			        .requestResponse("01 15 1", input.getKey())
			        .build();

			workflow.start(connection, query,optional);

			WorkflowFinalizer.finalizeAfter(workflow,1000);

			Assertions.assertThat(lifecycle.isErrorOccurred()).isTrue();
			Assertions.assertThat(lifecycle.getMessage()).isEqualTo(input.getValue());
		}
	}
}
