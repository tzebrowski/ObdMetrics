package org.obd.metrics.api;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Init.Header;
import org.obd.metrics.api.model.Init.Protocol;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.connection.MockAdapterConnection;

public class SupportedPIDsTest {

	@Test
	public void giulia_gme_2_0() throws IOException, InterruptedException {

		final SimpleLifecycle simpleLifecycle = new SimpleLifecycle();
		
		//Create an instance of DataCollector that receives the OBD Metrics
		final DataCollector collector = new DataCollector();

		//Getting the Workflow instance for mode 22
		final Workflow workflow = SimpleWorkflowFactory.getWorkflow(simpleLifecycle, collector);
		
		//Query for specified PID's like: Engine coolant temperature
		final Query query = Query.builder()
		        .pid(6008l) // Coolant
		        .pid(6004l) // RPM
		        .pid(6007l) // Intake temp
		        .pid(6015l)// Oil temp
		        .pid(603l) // Spark Advance
		        .build();

		//Create an instance of mock connection with additional commands and replies 
		final MockAdapterConnection connection = MockAdapterConnection.builder()
		        .requestResponse("0100", "4100BE3DA813410098180001")
		        .requestResponse("0120", "4120801FB011412080018001")
		        .requestResponse("0140", "4140FED09081414040800000")
		        .requestResponse("0160", "416001214000")
		        .requestResponse("0180", "NODATA")
		        .requestResponse("01A0", "NODATA")
		        .requestResponse("01C0", "NODATA")
		// Set read timeout for every character,e.g: inputStream.read(), we want to ensure that initial timeout will decrease during the tests			        
		        .readTimeout(1) //
		        .build();
		
		// Set target frequency
		final int targetCommandFrequency = 4;

		// Enable adaptive timing
		final Adjustments optional = Adjustments
		        .builder()
		        .vehicleCapabilitiesReadingEnabled(Boolean.TRUE)
		        .adaptiveTiming(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.TRUE)
		                .checkInterval(10)
		                .commandFrequency(targetCommandFrequency)
		                .build())
		        .cacheConfig(CachePolicy.builder().resultCacheEnabled(Boolean.FALSE).build())
		        .build();

		final Init init = Init.builder()
		        .delayAfterInit(0)
		        .header(Header.builder()
		        		.mode("22").header("DA10F1").build())
				.header(Header.builder()
						.mode("01").header("DB33F1").build())
		        .protocol(Protocol.CAN_29)
		        .sequence(DefaultCommandGroup.INIT).build();
		
		//Start background threads, that call the adapter,decode the raw data, and populates OBD metrics
		workflow.start(connection, query,init,optional);

		// Starting the workflow completion job, it will end workflow after given period of time (helper method)
		WorkflowFinalizer.finalizeAfter(workflow, 1000);
		
		// Ensure we receive AT command
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();
		
		
		Assertions.assertThat(simpleLifecycle.getCapabilities())
			.isNotNull()
			.containsExactly(
				"22", "01", "02", "03", "04", 
				"05", "06", "28", "07", "09", 
				"1c", "1f", "31", "10", "32", 
				"11", "34", "13", "35", "14", 
				"15", "39", "19", "2a", "2b", 
				"0a", "0b", "0c", "0d", "0e", 
				"0f", "20");
	}
	

	@Test
	public void med17_vw() throws IOException, InterruptedException {

		final SimpleLifecycle simpleLifecycle = new SimpleLifecycle();
		
		//Create an instance of DataCollector that receives the OBD Metrics
		final DataCollector collector = new DataCollector();

		//Getting the Workflow instance for mode 22
		final Workflow workflow = SimpleWorkflowFactory.getWorkflow(simpleLifecycle, collector);
		
		//Query for specified PID's like: Engine coolant temperature
		final Query query = Query.builder()
		        .pid(6008l) // Coolant
		        .build();

		//Create an instance of mock connection with additional commands and replies 
		final MockAdapterConnection connection = MockAdapterConnection.builder()
		        .requestResponse("0100", "4100BE3EA813")
		        .requestResponse("0120", "4120A005B011")
		        .requestResponse("0140", "4140FED00400")
		        .requestResponse("0160", "NODATA")
		        .requestResponse("0180", "NODATA")
		        .requestResponse("01A0", "NODATA")
		        .requestResponse("01C0", "NODATA")
		// Set read timeout for every character,e.g: inputStream.read(), we want to ensure that initial timeout will decrease during the tests			        
		        .readTimeout(1) //
		        .build();
		
		// Set target frequency
		final int targetCommandFrequency = 4;

		// Enable adaptive timing
		final Adjustments optional = Adjustments
		        .builder()
		        .vehicleCapabilitiesReadingEnabled(Boolean.TRUE)
		        .adaptiveTiming(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.TRUE)
		                .checkInterval(10)
		                .commandFrequency(targetCommandFrequency)
		                .build())
		        .cacheConfig(CachePolicy.builder().resultCacheEnabled(Boolean.FALSE).build())
		        .build();

		final Init init = Init.builder()
		        .delayAfterInit(0)
		        .header(Header.builder()
		        		.mode("22").header("DA10F1").build())
				.header(Header.builder()
						.mode("01").header("DB33F1").build())
		        .protocol(Protocol.CAN_29)
		        .sequence(DefaultCommandGroup.INIT).build();
		
		//Start background threads, that call the adapter,decode the raw data, and populates OBD metrics
		workflow.start(connection, query,init,optional);

		// Starting the workflow completion job, it will end workflow after given period of time (helper method)
		WorkflowFinalizer.finalizeAfter(workflow, 1000);
		
		// Ensure we receive AT command
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();
		
		
		Assertions.assertThat(simpleLifecycle.getCapabilities())
			.isNotNull()
			.containsExactly(
					"11", "01", "13", "02", "03", 
					"14", "04", "15", "05", "16",
					"06", "07", "09", "0a", "0b", 
					"1c", "0c", "0d", "0e", "1f", 
					"0f", "10");
	}
}
