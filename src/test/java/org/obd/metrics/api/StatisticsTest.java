package org.obd.metrics.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidRegistry;
import org.obd.metrics.statistics.MetricStatistics;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StatisticsTest {

	@Test
	public void mode01WorkflowTest() throws IOException, InterruptedException {

		final DataCollector collector = new DataCollector();
		final Workflow workflow = SimpleWorkflowFactory.getMode01Workflow(collector);

		final Set<Long> ids = new HashSet<>();
		ids.add(6l); // Engine coolant temperature
		ids.add(12l); // Intake manifold absolute pressure
		ids.add(13l); // Engine RPM
		ids.add(16l); // Intake air temperature
		ids.add(18l); // Throttle position
		ids.add(14l); // Vehicle speed

		final MockConnection connection = MockConnection.builder()
		        .commandReply("0100", "4100be3ea813")
		        .commandReply("0200", "4140fed00400")
		        .commandReply("01 0B 0C 11 0D 0F 05", "00e0:410bff0c00001:11000d000f00052:00aaaaaaaaaaaa").build();

		workflow.start(connection, Adjustements
		        .builder()
		        .filter(ids)
		        .batchEnabled(true)
		        .build());

		final Callable<String> end = () -> {
			Thread.sleep(1 * 2000);
			log.info("Ending the process of collecting the data");
			workflow.stop();
			return "end";
		};

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(1);
		newFixedThreadPool.invokeAll(Arrays.asList(end));
		newFixedThreadPool.shutdown();

		final PidRegistry pids = workflow.getPidRegistry();

		final PidDefinition engineTemp = pids.findBy(6l);
		Assertions.assertThat(engineTemp.getPid()).isEqualTo("05");

		Assertions.assertThat(workflow.getStatisticsRegistry().getRatePerSec(engineTemp)).isGreaterThan(10d);
		Assertions.assertThat(workflow.getStatisticsRegistry().getRatePerSec(pids.findBy(12l))).isGreaterThan(10d);

	}

	@Test
	public void genericWorkflowTest() throws IOException, InterruptedException {

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
		        .filter(ids).build());

		final Callable<String> end = () -> {
			Thread.sleep(1 * 1500);
			log.info("Ending the process of collecting the data");
			workflow.stop();
			return "end";
		};

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(1);
		newFixedThreadPool.invokeAll(Arrays.asList(end));
		newFixedThreadPool.shutdown();

		final PidRegistry pids = workflow.getPidRegistry();

		PidDefinition pid8l = pids.findBy(8l);
		MetricStatistics stat8l = workflow.getStatisticsRegistry().findBy(pid8l);
		Assertions.assertThat(stat8l).isNotNull();

		PidDefinition pid4l = pids.findBy(4l);
		MetricStatistics stat4L = workflow.getStatisticsRegistry().findBy(pid4l);
		Assertions.assertThat(stat4L).isNotNull();

		Assertions.assertThat(stat4L.getMax()).isEqualTo(762);
		Assertions.assertThat(stat4L.getMin()).isEqualTo(762);
		Assertions.assertThat(stat4L.getMedian()).isEqualTo(762);

		Assertions.assertThat(stat8l.getMax()).isEqualTo(-1);
		Assertions.assertThat(stat8l.getMin()).isEqualTo(-1);
		Assertions.assertThat(stat8l.getMedian()).isEqualTo(-1);

		Assertions.assertThat(workflow.getStatisticsRegistry().getRatePerSec(pid8l)).isGreaterThan(10d);
		Assertions.assertThat(workflow.getStatisticsRegistry().getRatePerSec(pid4l)).isGreaterThan(10d);
	}

}
