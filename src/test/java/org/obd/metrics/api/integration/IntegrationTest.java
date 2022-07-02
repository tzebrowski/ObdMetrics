package org.obd.metrics.api.integration;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;
import org.obd.metrics.api.Workflow;
import org.obd.metrics.api.WorkflowFinalizer;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Pids;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.diagnostic.RateType;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.AdapterConnection;
import org.obd.metrics.transport.TcpAdapterConnection;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IntegrationTest {
	// [01, 03, 04, 05, 06, 07, 0b, 0c, 0d, 0e, 0f, 10, 11, 13, 15, 1c],
	// raw=4100be3fa811]
	
	@Test
	public void tcpConnection() throws IOException, InterruptedException, ExecutionException {
		final AdapterConnection connection = TcpAdapterConnection.of("192.168.0.10", 35000);
		final DataCollector collector = new DataCollector();

		int commandFrequency = 6;
		final Workflow workflow = Workflow
		        .instance()
		        .pids(Pids.DEFAULT)
		        .observer(collector)
		        .initialize();

		workflow.getPidRegistry().findBy(7l).setPriority(0);
		workflow.getPidRegistry().findBy(8l).setPriority(0);
		workflow.getPidRegistry().findBy(17l).setPriority(0);
		workflow.getPidRegistry().findBy(22l).setPriority(0);
		workflow.getPidRegistry().findBy(6l).setPriority(0);
		workflow.getPidRegistry().findBy(12l).setPriority(0);
		workflow.getPidRegistry().findBy(13l).setPriority(0);
		workflow.getPidRegistry().findBy(16l).setPriority(0);

		workflow.getPidRegistry().findBy(18l).setPriority(1);
		workflow.getPidRegistry().findBy(14l).setPriority(1);
		workflow.getPidRegistry().findBy(15l).setPriority(1);

		final Query query = Query.builder()
		        .pid(7l) // Short trims
		        .pid(8l) // Long trim
		        .pid(17l) // MAF
		        .pid(22l) // Oxygen sensor
		        .pid(6l) // Engine coolant temperature
		        .pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        .pid(16l) // Intake air temperature
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .pid(15l) // Timing advance
		        .pid(9000l) // Battery voltage
		        .build();

		final Adjustments optional = Adjustments
		        .builder()
		        .adaptiveTiming(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.TRUE)
		                .checkInterval(5000)
		                .commandFrequency(commandFrequency)
		                .build())
		        .producerPolicy(ProducerPolicy.builder()
		                .priorityQueueEnabled(Boolean.TRUE)
		                .lowPriorityCommandFrequencyDelay(2000).build())
		        .batchEnabled(true)
		        .build();

		workflow.start(connection, query, Init.DEFAULT, optional);

		WorkflowFinalizer.finalizeAfter(workflow, 270000, () -> false);

		final PidDefinitionRegistry rpm = workflow.getPidRegistry();

		PidDefinition measuredPID = rpm.findBy(13l);
		double ratePerSec = workflow.getDiagnostics().rate().findBy(RateType.MEAN, measuredPID).get().getValue();

		log.info("Rate:{}  ->  {}", measuredPID, ratePerSec);

		Assertions.assertThat(ratePerSec).isGreaterThanOrEqualTo(commandFrequency);
	}
}
