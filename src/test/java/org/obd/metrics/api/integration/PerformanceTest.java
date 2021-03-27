package org.obd.metrics.api.integration;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;
import org.obd.metrics.api.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.Adjustements;
import org.obd.metrics.api.PidSpec;
import org.obd.metrics.api.ProducerPolicy;
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

		final Set<Long> ids = new HashSet<>();
		ids.add(6l); // Engine coolant temperature
		ids.add(12l); // Intake manifold absolute pressure
		ids.add(13l); // Engine RPM
		ids.add(16l); // Intake air temperature
		ids.add(18l); // Throttle position
		ids.add(14l); // Vehicle speed
		ids.add(15l); // Timing advance

		Adjustements adjustements = Adjustements
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
		        .filter(ids).build();

		workflow.start(connection, adjustements);

		final Callable<String> end = () -> {
			Thread.sleep(1 * 270000);
			log.info("Ending the process of collecting the data");
			workflow.stop();
			return "end";
		};

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(1);
		newFixedThreadPool.invokeAll(Arrays.asList(end));

		final PidRegistry pids = workflow.getPidRegistry();

		PidDefinition measuredPID = pids.findBy(13l);
		double ratePerSec = workflow.getStatisticsRegistry().getRatePerSec(measuredPID);

		log.info("Rate:{}  ->  {}", measuredPID, ratePerSec);

		Assertions.assertThat(ratePerSec).isGreaterThanOrEqualTo(commandFrequency);

		newFixedThreadPool.shutdown();
	}
}
