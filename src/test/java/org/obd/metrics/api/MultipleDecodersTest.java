package org.obd.metrics.api;

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
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidRegistry;
import org.obd.metrics.statistics.MetricStatistics;
import org.obd.metrics.statistics.StatisticsRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MultipleDecodersTest {

	@Test
	public void t0() throws IOException, InterruptedException, ExecutionException {

		final Workflow workflow = SimpleWorkflowFactory.getMode01Workflow(new DataCollector());
		

		final Set<Long> filter = new HashSet<>();
		filter.add(22l);//
		filter.add(23l);//

		final MockConnection connection = MockConnection.builder()
		        .commandReply("0100", "4100be3ea813")
		        .commandReply("0200", "4140fed00400")
		        .commandReply("0115", "4115FFff").build();

		workflow.start(WorkflowContext
		        .builder()
		        .connection(connection)
		        .filter(filter).build());

		final Callable<String> end = () -> {
			Thread.sleep(500);
			log.info("Ending the process of collecting the data");
			workflow.stop();
			return "end";
		};

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(3);
		newFixedThreadPool.invokeAll(Arrays.asList(end));
		newFixedThreadPool.shutdown();

		final PidRegistry pids = workflow.getPidRegistry();
		PidDefinition pid22 = pids.findBy(22l);
		StatisticsRegistry statistics = workflow.getStatisticsRegistry();
		MetricStatistics stat22 = statistics.findBy(pid22);
		Assertions.assertThat(stat22).isNotNull();

		PidDefinition pid23 = pids.findBy(23l);
		MetricStatistics stat23 = statistics.findBy(pid23);
		Assertions.assertThat(stat23).isNotNull();

		Assertions.assertThat(stat22.getMax()).isEqualTo(10L);
		Assertions.assertThat(stat22.getMin()).isEqualTo(10L);
		Assertions.assertThat(stat22.getMedian()).isEqualTo(10L);

		Assertions.assertThat(stat23.getMax()).isEqualTo(1);
		Assertions.assertThat(stat23.getMin()).isEqualTo(1);
		Assertions.assertThat(stat23.getMedian()).isEqualTo(1);

	}
}
