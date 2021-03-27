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
import org.obd.metrics.codec.GeneratorSpec;
import org.obd.metrics.command.group.AlfaMed17CommandGroup;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidRegistry;
import org.obd.metrics.pid.Urls;
import org.obd.metrics.statistics.MetricStatistics;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataGeneratorTest {

	@Test
	public void generatorTest() throws IOException, InterruptedException {
		final Workflow workflow = SimpleWorkflowFactory.getMode22Workflow(new DataCollector());

		final Set<Long> ids = new HashSet<>();
		ids.add(8l); // Coolant
		ids.add(4l); // RPM
		ids.add(7l); // Intake temp
		ids.add(15l);// Oil temp
		ids.add(3l); // Spark Advance

		final MockConnection connection = MockConnection.builder()
		        .commandReply("221003", "62100340")
		        .commandReply("221000", "xxxxxxxxxxxxxx")
		        .commandReply("221935", "xxxxxxxxxxxxxx")
		        .commandReply("22194f", "xxxxxxxxxxxxxx")
		        .commandReply("221812", "")
		        .build();

		workflow.start(connection, Adjustements
		        .builder()
		        .generator(GeneratorSpec.builder().increment(1.0).enabled(true).build())
		        .filter(ids).build());

		final Callable<String> end = () -> {
			Thread.sleep(1 * 500);
			log.info("Ending the process of collecting the data");
			workflow.stop();
			return "end";
		};

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(1);
		newFixedThreadPool.invokeAll(Arrays.asList(end));
		newFixedThreadPool.shutdown();

		final PidRegistry pids = workflow.getPidRegistry();

		PidDefinition pid8l = pids.findBy(8l);

		Assertions.assertThat(workflow.getStatisticsRegistry().getRatePerSec(pid8l)).isGreaterThan(0);

		MetricStatistics stats = workflow.getStatisticsRegistry().findBy(pid8l);

		Assertions.assertThat(stats.getMax()).isGreaterThan(stats.getMin());
		Assertions.assertThat(stats.getMin()).isLessThan((long) stats.getMedian());
		Assertions.assertThat(stats.getMedian()).isLessThan(stats.getMax()).isGreaterThan(stats.getMin());
	}

	@Test
	public void defaultIncrementTest() throws IOException, InterruptedException {

		final Workflow workflow = WorkflowFactory.generic()
		        .pidSpec(PidSpec
		                .builder()
		                .initSequence(AlfaMed17CommandGroup.CAN_INIT_NO_DELAY)
		                .pidFile(Urls.resourceToUrl("alfa.json")).build())
		        .observer(new DataCollector())
		        .initialize();

		final Set<Long> ids = new HashSet<>();
		ids.add(8l); // Coolant
		ids.add(4l); // RPM
		ids.add(7l); // Intake temp
		ids.add(15l);// Oil temp
		ids.add(3l); // Spark Advance

		final MockConnection connection = MockConnection.builder()
		        .commandReply("221003", "62100340")
		        .commandReply("221000", "xxxxxxxxxxxxxx")
		        .commandReply("221935", "xxxxxxxxxxxxxx")
		        .commandReply("22194f", "xxxxxxxxxxxxxx")
		        .commandReply("221812", "")
		        .build();

		workflow.start(connection, Adjustements
		        .builder()
		        .generator(GeneratorSpec.builder().enabled(true).build())
		        .filter(ids).build());

		final Callable<String> end = () -> {
			Thread.sleep(1 * 500);
			log.info("Ending the process of collecting the data");
			workflow.stop();
			return "end";
		};

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(1);
		newFixedThreadPool.invokeAll(Arrays.asList(end));
		newFixedThreadPool.shutdown();

		final PidRegistry pids = workflow.getPidRegistry();

		PidDefinition pid8l = pids.findBy(8l);

		Assertions.assertThat(workflow.getStatisticsRegistry().getRatePerSec(pid8l)).isGreaterThan(0);

		MetricStatistics stats = workflow.getStatisticsRegistry().findBy(pid8l);

		Assertions.assertThat(stats.getMax()).isGreaterThan(stats.getMin());
		Assertions.assertThat(stats.getMin()).isLessThan((long) stats.getMedian());
		Assertions.assertThat(stats.getMedian()).isLessThan(stats.getMax()).isGreaterThan(stats.getMin());
	}

	@Test
	public void smartTest() throws IOException, InterruptedException {

		final DataCollector collector = new DataCollector();
		final Workflow workflow = WorkflowFactory.generic()
		        .pidSpec(PidSpec
		                .builder()
		                .initSequence(AlfaMed17CommandGroup.CAN_INIT_NO_DELAY)
		                .pidFile(Urls.resourceToUrl("alfa.json")).build())
		        .observer(collector)
		        .initialize();

		final PidRegistry pidRegistry = workflow.getPidRegistry();
		pidRegistry.register(new PidDefinition(10001l, 2, "((A *256 ) +B)/4", "22", "2000", "rpm", "Engine RPM",
		        0, 1, PidDefinition.Type.DOUBLE));
		pidRegistry.register(new PidDefinition(10002l, 2, "((A *256 ) +B)/4", "22", "2002", "rpm", "Engine RPM",
		        2, 5, PidDefinition.Type.DOUBLE));
		pidRegistry.register(new PidDefinition(10003l, 2, "((A *256 ) +B)/4", "22", "2004", "rpm", "Engine RPM",
		        5, 20, PidDefinition.Type.DOUBLE));

		pidRegistry.register(new PidDefinition(10004l, 2, "((A *256 ) +B)/4", "22", "2006", "rpm", "Engine RPM",
		        20, 100, PidDefinition.Type.DOUBLE));

		pidRegistry.register(new PidDefinition(10005l, 2, "((A *256 ) +B)/4", "22", "2008", "rpm", "Engine RPM",
		        1000, 7000, PidDefinition.Type.DOUBLE));

		final Set<Long> ids = new HashSet<>();
		ids.add(10001l);
		ids.add(10002l);
		ids.add(10003l);
		ids.add(10004l);
		ids.add(10005l);

		final MockConnection connection = MockConnection.builder()
		        .commandReply("222000", "6220000BEA")
		        .commandReply("222002", "6220020BEA")
		        .commandReply("222004", "6220040BEA")
		        .commandReply("222006", "6220060BEA")
		        .commandReply("222008", "6220080BEA")
		        .build();

		workflow.start(connection, Adjustements
		        .builder()
		        .generator(GeneratorSpec.builder().smart(true).enabled(true).build())
		        .filter(ids).build());

		final Callable<String> end = () -> {
			Thread.sleep(1 * 500);
			log.info("Ending the process of collecting the data");
			workflow.stop();
			return "end";
		};

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(1);
		newFixedThreadPool.invokeAll(Arrays.asList(end));

		newFixedThreadPool.shutdown();

		List<ObdMetric> collection = collector.findMetricsBy(workflow.getPidRegistry().findBy(10002l));
		Assertions.assertThat(collection.isEmpty()).isFalse();
		Assertions.assertThat(collection.iterator().next().getValue()).isInstanceOf(Double.class);

		collection = collector.findMetricsBy(workflow.getPidRegistry().findBy(10001l));
		Assertions.assertThat(collection.isEmpty()).isFalse();
		Assertions.assertThat(collection.iterator().next().getValue()).isInstanceOf(Double.class);

		collection = collector.findMetricsBy(workflow.getPidRegistry().findBy(10003l));
		Assertions.assertThat(collection.isEmpty()).isFalse();
		Assertions.assertThat(collection.iterator().next().getValue()).isInstanceOf(Double.class);

	}
}
