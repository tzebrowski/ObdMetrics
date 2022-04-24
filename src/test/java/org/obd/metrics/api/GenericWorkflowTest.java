package org.obd.metrics.api;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;
import org.obd.metrics.ObdMetric;
import org.obd.metrics.connection.SimpleMockConnection;
import org.obd.metrics.diagnostic.RateType;
import org.obd.metrics.pid.PidDefinition;


public class GenericWorkflowTest {

	@Test
	public void recieveReplyTest() throws IOException, InterruptedException {
		// Create an instance of DataCollector that receives the OBD Metrics
		DataCollector collector = new DataCollector();

		// Create an instance of the Mode 22 Workflow
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(collector);

		// Query for specified PID's like RPM
		Query query = Query.builder()
		        .pid(6008l) // Coolant
		        .pid(6004l) // RPM
		        .pid(6007l) // Intake temp
		        .pid(6015l)// Oil temp
		        .pid(6003l) // Spark Advance
		        .build();

		// Create an instance of mocked connection with additional commands and replies
		SimpleMockConnection connection = SimpleMockConnection.builder()
		        .commandReply("221003", "62100340")
		        .commandReply("221000", "6210000BEA")
		        .commandReply("221935", "62193540")
		        .commandReply("22194f", "62194f2d85")
		        .build();

		// Extra settings for collecting process like command frequency 14/sec
		Adjustments optional = Adjustments.builder()
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

		PidDefinition rpm = workflow.getPidRegistry().findBy(6004l);

		// Workflow completion thread, it will end workflow after some period of time
		// (helper method)
		WorkflowFinalizer.finalizeAfter(workflow, 1000, ()-> workflow.getDiagnostics().rate().findBy(RateType.MEAN,rpm).get().getValue() > 5);
		
		// Ensure we receive AT command as well
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();

		// Gets the metric
		ObdMetric metric = collector.findSingleMetricBy(rpm);
		Assertions.assertThat(metric).isNotNull();
		Assertions.assertThat(metric.valueToDouble()).isEqualTo(762.5);
	}
}
