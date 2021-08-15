package org.obd.metrics.api;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;


public class GenericWorkflowTest {

	@Test
	public void recieveReplyTest() throws IOException, InterruptedException {
		// Create an instance of DataCollector that receives the OBD Metrics
		var collector = new DataCollector();

		// Create an instance of the Mode 22 Workflow
		var workflow = SimpleWorkflowFactory.getMode22Workflow(collector);

		// Query for specified PID's like RPM
		var query = Query.builder()
		        .pid(8l) // Coolant
		        .pid(4l) // RPM
		        .pid(7l) // Intake temp
		        .pid(15l)// Oil temp
		        .pid(3l) // Spark Advance
		        .build();

		// Create an instance of mocked connection with additional commands and replies
		var connection = MockConnection.builder()
		        .commandReply("221003", "62100340")
		        .commandReply("221000", "6210000BEA")
		        .commandReply("221935", "62193540")
		        .commandReply("22194f", "62194f2d85")
		        .build();

		// Extra settings for collecting process like command frequency 14/sec
		var optional = Adjustments.builder()
		        .initDelay(0)
		        .adaptiveTiming(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.TRUE)
		                .checkInterval(20)// 20ms
		                .commandFrequency(14).build())
		        .producerPolicy(ProducerPolicy.builder().priorityQueueEnabled(false).build())
		        .build();

		// Start background threads, that call the adapter,decode the raw data, and
		// populates OBD metrics
		workflow.start(connection, query, optional);

		var rpm = workflow.getPidRegistry().findBy(4l);

		// Workflow completion thread, it will end workflow after some period of time
		// (helper method)
		WorkflowFinalizer.finalizeAfter(workflow, 1000, ()-> workflow.getStatisticsRegistry().getRatePerSec(rpm) > 5);
		
		
		// Ensure we receive AT command as well
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();

		// Gets the metric
		var metric = collector.findSingleMetricBy(rpm);
		Assertions.assertThat(metric).isNotNull();
		Assertions.assertThat(metric.valueToDouble()).isEqualTo(762.5);
	}
}
