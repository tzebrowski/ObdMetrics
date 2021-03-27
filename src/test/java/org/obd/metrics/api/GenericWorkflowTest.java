package org.obd.metrics.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;
import org.obd.metrics.ObdMetric;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.statistics.StatisticsRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GenericWorkflowTest {

	@Test
	public void recieveReplyTest() throws IOException, InterruptedException {

		final DataCollector collector = new DataCollector();
		final Workflow workflow = SimpleWorkflowFactory.getMode22Workflow(collector);

		final Set<Long> ids = new HashSet<>();
		ids.add(8l); // Coolant
		ids.add(4l); // RPM
		ids.add(7l); // Intake temp
		ids.add(15l);// Oil temp
		ids.add(3l); // Spark Advance

		final MockConnection connection = MockConnection.builder()
		        .commandReply("221003", "62100340")
		        .commandReply("221000", "6210000BEA")
		        .commandReply("221935", "62193540")
		        .commandReply("22194f", "62194f2d85")
		        .build();

		workflow.start(connection, Adjustements
		        .builder()
		        .adaptiveTiming(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.TRUE)
		                .checkInterval(20)// 20ms
		                .commandFrequency(14).build())
		        .producerPolicy(ProducerPolicy.builder().priorityQueueEnabled(false).build())
		        .filter(ids).build());

		PidDefinition pid = workflow.getPidRegistry().findBy(4l);
		StatisticsRegistry statisticsRegistry = workflow.getStatisticsRegistry();
		final Callable<String> end = () -> {
			final ConditionalSleep conditionalSleep = ConditionalSleep
			        .builder()
			        .condition(() -> {
				        return statisticsRegistry.getRatePerSec(pid) > 5;
			        })
			        .particle(50l)
			        .build();

			final long sleep = conditionalSleep.sleep(1000);
			log.info("Ending the process of collecting the data. Sleep time: {}", sleep);
			workflow.stop();
			return "end";
		};

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(1);
		newFixedThreadPool.invokeAll(Arrays.asList(end));
		newFixedThreadPool.shutdown();

		// Ensure we receive AT command as well
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();

		final List<ObdMetric> collection = collector.findMetricsBy(pid);
		Assertions.assertThat(collection.isEmpty()).isFalse();
		Assertions.assertThat(collection.iterator().next().valueToDouble()).isEqualTo(762.5);
	}
}
