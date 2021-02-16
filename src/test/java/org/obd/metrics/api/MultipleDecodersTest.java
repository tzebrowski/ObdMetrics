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
import org.obd.metrics.DummyObserver;
import org.obd.metrics.command.group.Mode1CommandGroup;
import org.obd.metrics.connection.MockedConnection;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidRegistry;
import org.obd.metrics.statistics.Statistics;
import org.obd.metrics.statistics.StatisticsAccumulator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MultipleDecodersTest {

	@Test
	public void t0() throws IOException, InterruptedException, ExecutionException {

		final Workflow workflow = Workflow.mode1().equationEngine("JavaScript")
				.ecuSpecific(EcuSpecific
						.builder()
						.initSequence(Mode1CommandGroup.INIT_NO_DELAY)
						.pidFile("mode01.json").build())
				.observer(new DummyObserver()).build();

		final Set<Long> filter = new HashSet<>();
		filter.add(22l);//
		filter.add(23l);//
		
		MockedConnection connection = MockedConnection.builder().
				parameter("0100", "4100be3ea813").
				parameter("0200", "4140fed00400").
				parameter("0115", "4115FFff").build();
				
		workflow.connection(connection).filter(filter).batch(false).start();

		final Callable<String> end = () -> {
			Thread.sleep(1500);
			log.info("Ending the process of collecting the data");
			workflow.stop();
			return "end";
		};

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(3);
		newFixedThreadPool.invokeAll(Arrays.asList(end));
		newFixedThreadPool.shutdown();

		final PidRegistry pids = workflow.getPids();
		PidDefinition pid22 = pids.findBy(22l);
		StatisticsAccumulator statistics = workflow.getStatistics();
		Statistics stat22 = statistics.findBy(pid22);
		Assertions.assertThat(stat22).isNotNull();

		PidDefinition pid23 = pids.findBy(23l);
		Statistics stat23 = statistics.findBy(pid23);
		Assertions.assertThat(stat23).isNotNull();

		Assertions.assertThat(statistics.getRatePerSec(pid22)).isGreaterThan(10);
		Assertions.assertThat(statistics.getRatePerSec(pid23)).isGreaterThan(10);

		Assertions.assertThat(stat22.getMax()).isEqualTo(10L);
		Assertions.assertThat(stat22.getMin()).isEqualTo(10L);
		Assertions.assertThat(stat22.getMedian()).isEqualTo(10L);

		Assertions.assertThat(stat23.getMax()).isEqualTo(1);
		Assertions.assertThat(stat23.getMin()).isEqualTo(1);
		Assertions.assertThat(stat23.getMedian()).isEqualTo(1);

	}
}
