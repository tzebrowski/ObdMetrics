package org.obd.metrics.api.integration;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.CompletionThread;
import org.obd.metrics.DataCollector;
import org.obd.metrics.api.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.Adjustements;
import org.obd.metrics.api.PidSpec;
import org.obd.metrics.api.ProducerPolicy;
import org.obd.metrics.api.Query;
import org.obd.metrics.api.Workflow;
import org.obd.metrics.api.WorkflowFactory;
import org.obd.metrics.command.group.Mode1CommandGroup;
import org.obd.metrics.connection.StreamConnection;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PerformanceTest {

	// 11 = 10
	@Test
	public void t0() throws IOException, InterruptedException, ExecutionException {
		final StreamConnection connection = BluetoothConnection.openConnection();
		final DataCollector collector = new DataCollector();

		int commandFrequency = 6;
		final Workflow workflow = WorkflowFactory
		        .mode1()
		        .pidSpec(PidSpec
		                .builder()
		                .initSequence(Mode1CommandGroup.INIT)
		                .pidFile(Thread.currentThread().getContextClassLoader().getResource("mode01.json")).build())
		        .observer(collector)
		        .initialize();

		final Query query = Query.builder()
		        .pid(6l)  // Engine coolant temperature
		        .pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        .pid(16l) // Intake air temperature
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .pid(15l) // Timing advance
		        .build();

		final Adjustements optional = Adjustements
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

		workflow.start(connection, query, optional);

		CompletionThread.setup(workflow, 270000);

		final PidRegistry rpm = workflow.getPidRegistry();

		PidDefinition measuredPID = rpm.findBy(13l);
		double ratePerSec = workflow.getStatisticsRegistry().getRatePerSec(measuredPID);

		log.info("Rate:{}  ->  {}", measuredPID, ratePerSec);

		Assertions.assertThat(ratePerSec).isGreaterThanOrEqualTo(commandFrequency);

	}
}
