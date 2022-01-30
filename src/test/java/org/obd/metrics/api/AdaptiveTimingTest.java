package org.obd.metrics.api;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;
import org.obd.metrics.diagnostic.RateType;
import org.obd.metrics.pid.PidDefinition;

public class AdaptiveTimingTest {

	@Test
	public void adaptiveTimingTest() throws IOException, InterruptedException {

		//Create an instance of DataCollector that receives the OBD Metrics
		final DataCollector collector = new DataCollector();

		//Getting the Workflow instance for mode 22
		final Workflow workflow = SimpleWorkflowFactory.getMode22Workflow(collector);
		
		//Query for specified PID's like: Engine coolant temperature
		final Query query = Query.builder()
		        .pid(8l) // Coolant
		        .pid(4l) // RPM
		        .pid(7l) // Intake temp
		        .pid(15l)// Oil temp
		        .pid(3l) // Spark Advance
		        .build();

		//Create an instance of mock connection with additional commands and replies 
		final MockConnection connection = MockConnection.builder()
		        .commandReply("221003", "62100340")
		        .commandReply("221000", "6210000BEA")
		        .commandReply("221935", "62193540")
		        .commandReply("22194f", "62194f2d85")
		// Set read timeout for every character,e.g: inputStream.read(), we want to ensure that initial timeout will be decrease during the tests			        
		        .readTimeout(1) //
		        .build();
		
		// Set target frequency
		final int targetCommandFrequency = 4;

		// Enable adaptive timing
		final Adjustments optional = Adjustments
		        .builder()
		        .initDelay(0)
		        .adaptiveTiming(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.TRUE)
		                .checkInterval(10)// 20ms
		                .commandFrequency(targetCommandFrequency)
		                .build())
		        .build();

		//Start background threads, that call the adapter,decode the raw data, and populates OBD metrics
		workflow.start(connection, query, optional);
		
		PidDefinition rpm = workflow.getPidRegistry().findBy(4l);

		// Starting the workflow completion job, it will end workflow after some period of time (helper method)
		WorkflowFinalizer.finalizeAfter(workflow, 1500, ()-> workflow.getDiagnostics().getRateBy(RateType.MEAN,rpm).get().getValue() > targetCommandFrequency + 2);
		
		// Ensure we receive AT command
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();
		
		// Ensure target command frequency is on the expected level
		double ratePerSec = workflow.getDiagnostics().getRateBy(RateType.MEAN,rpm).get().getValue();
		Assertions.assertThat(ratePerSec)
		        .isGreaterThanOrEqualTo(targetCommandFrequency);
	}
}
