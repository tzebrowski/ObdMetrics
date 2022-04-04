package org.obd.metrics.api;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;
import org.obd.metrics.ObdMetric;
import org.obd.metrics.connection.SimpleMockConnection;
import org.obd.metrics.pid.PidDefinition;

public class BatteryVoltageTest {

	@Test
	public void case_01() throws IOException, InterruptedException {

		// Create an instance of DataCollector that receives the OBD Metrics
		DataCollector collector = new DataCollector();

		// Getting the Workflow instance for mode 01
		Workflow workflow = SimpleWorkflowFactory.getMode01WorkflowExtended(collector);

		// Query for specified PID's like: Engine coolant temperature
		Query query = Query.builder()
		        .pid(6l) // Engine coolant temperature
		        .pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        .pid(16l) // Intake air temperature
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .pid(9000l) // Battery voltage
		        .build();

		// Create an instance of mock connection with additional commands and replies
		SimpleMockConnection connection = SimpleMockConnection.builder()
		        .commandReply("ATRV", "14.1v")
		        .commandReply("0100", "4100be3ea813")
		        .commandReply("0200", "4140fed00400")
		        .commandReply("01 0B 0C 11 0D 0F 05", "00e0:410bff0c00001:11000d000f00052:00aaaaaaaaaaaa").build();

		// Enabling batch commands
		Adjustments optional = Adjustments
		        .builder()
		        .producerPolicy(ProducerPolicy.builder().priorityQueueEnabled(false).build())
		        .initDelay(0)
		        .batchEnabled(true)
		        .build();

		// Start background threads, that call the adapter,decode the raw data, and
		// populates OBD metrics
		workflow.start(connection, query, optional);

		// Starting the workflow completion job, it will end workflow after some period
		// of time (helper method)
		WorkflowFinalizer.finalizeAfter(workflow, 800);

		// Ensure we receive AT commands
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();

		final PidDefinition batteryVoltagePid = workflow.getPidRegistry().findBy(9000l);
		// Ensure pid was loaded from extra.json file
		Assertions.assertThat(batteryVoltagePid).overridingErrorMessage("no battery voltage pid found").isNotNull();
		
		// Find metrics for battery voltage
		final ObdMetric metric = collector.findSingleMetricBy(batteryVoltagePid);

		Assertions.assertThat(metric).overridingErrorMessage("no battery voltage metrics found").isNotNull();
		Assertions.assertThat(metric.getValue()).isInstanceOf(Double.class);
		Assertions.assertThat(metric.getValue()).isEqualTo(14.1);
	}
}
