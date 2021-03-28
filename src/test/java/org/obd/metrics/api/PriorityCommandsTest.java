package org.obd.metrics.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidRegistry;
import org.obd.metrics.statistics.StatisticsRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PriorityCommandsTest {

	@Test
	public void t0() throws IOException, InterruptedException {

		final Workflow workflow = SimpleWorkflowFactory.getMode01Workflow(new DataCollector());

		// more than 6 commands, so that we have 2 groups
		final Query query = Query.builder()
		        .pid(6l)  // Engine coolant temperature
		        .pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        .pid(16l) // Intake air temperature
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .pid(15l) // Timing advance
		        .build();

		final MockConnection connection = MockConnection.builder()
		        .commandReply("0100", "4100be3ea813")
		        .commandReply("0200", "4140fed00400")
		        .commandReply("01 05", "410500") // group 1
		        .commandReply("01 0B 0C 11 0D 0E 0F", "00e0:410bff0c00001:11000d000e800f2:00aaaaaaaaaaaa") // group 2
		        .build();

		Adjustments optional = Adjustments.builder()
		        .batchEnabled(true)
		        .producerPolicy(
		                ProducerPolicy.builder()
		                        .priorityQueueEnabled(Boolean.TRUE)
		                        .lowPriorityCommandFrequencyDelay(100)
		                        .build())
		        .build();
		
		workflow.start(connection, query, optional);

		final PidRegistry pidRegistry = workflow.getPidRegistry();
		final PidDefinition p1 = pidRegistry.findBy(6l);// Engine coolant temperature
		final PidDefinition p2 = pidRegistry.findBy(13l);// Engine RPM
		final StatisticsRegistry statisticsRegistry = workflow.getStatisticsRegistry();

		runCompletionThread(workflow, p1, p2);

		final double rate1 = statisticsRegistry.getRatePerSec(p1);
		final double rate2 = statisticsRegistry.getRatePerSec(p2);

		log.info("Pid: {}, rate: {}", p1.getDescription(), rate1);
		log.info("Pid: {}, rate: {}", p2.getDescription(), rate2);

		Assertions.assertThat(rate1).isGreaterThan(0);
		Assertions.assertThat(rate2).isGreaterThan(0);

		// coolant should have less RPS than RPM
		Assertions.assertThat(rate1).isLessThanOrEqualTo(rate2);
	}

	private void runCompletionThread(final Workflow workflow, final PidDefinition p1, final PidDefinition p2)
	        throws InterruptedException {

		final StatisticsRegistry statisticsRegistry = workflow.getStatisticsRegistry();

		final Callable<String> end = () -> {
			final ConditionalSleep conditionalSleep = ConditionalSleep.builder()
			        .condition(() -> {
				        final double r1 = statisticsRegistry.getRatePerSec(p1);
				        final double r2 = statisticsRegistry.getRatePerSec(p2);
				        return r1 > 0 && r2 > 0;
			        }).particle(50l).build();

			long sleep = conditionalSleep.sleep(1000);
			log.info("Ending the process of collecting the data. Sleep time: {}", sleep);
			workflow.stop();
			return "end";
		};

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(1);
		newFixedThreadPool.invokeAll(Arrays.asList(end));
		newFixedThreadPool.shutdown();
	}
}
