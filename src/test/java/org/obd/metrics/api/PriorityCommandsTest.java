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
import org.obd.metrics.Reply;
import org.obd.metrics.command.at.CustomATCommand;
import org.obd.metrics.command.group.Mode1CommandGroup;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidRegistry;
import org.obd.metrics.pid.Urls;
import org.obd.metrics.statistics.StatisticsRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PriorityCommandsTest {

	@Test
	public void t0() throws IOException, InterruptedException {

		final DataCollector collector = new DataCollector();
		final Workflow workflow = WorkflowFactory.mode1()
		        .pidSpec(PidSpec
		                .builder()
		                .initSequence(Mode1CommandGroup.INIT_NO_DELAY)
		                .pidFile(Urls.resourceToUrl("mode01.json")).build())
		        .observer(collector).initialize();

		//more than 6 commands, so that we have 2 groups
		final Set<Long> ids = new HashSet<>();
		ids.add(6l); // Engine coolant temperature
		ids.add(12l); // Intake manifold absolute pressure
		ids.add(13l); // Engine RPM
		ids.add(16l); // Intake air temperature
		ids.add(18l); // Throttle position
		ids.add(14l); // Vehicle speed
		ids.add(15l); // Timing advance

		final MockConnection connection = MockConnection.builder()
		        .commandReply("0100", "4100be3ea813")
		        .commandReply("0200", "4140fed00400")
		        .commandReply("01 05", "410500")//group 1
		        .commandReply("01 0B 0C 11 0D 0E 0F", "00e0:410bff0c00001:11000d000e800f2:00aaaaaaaaaaaa") //group 2
		        .build();

		workflow.start(WorkflowContext
		        .builder()
		        .batchEnabled(true)
		        .connection(connection)
		        .filter(ids)
		        .producerPolicy(
		                ProducerPolicy.builder()
		                        .priorityQueue(Boolean.TRUE)
		                        .lowPriorityCommandFrequencyDelay(100)
		                        .build())
		        .build());

		final Callable<String> end = () -> {
			Thread.sleep(4 * 500);
			log.info("Ending the process of collecting the data");
			workflow.stop();
			return "end";
		};

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(1);
		newFixedThreadPool.invokeAll(Arrays.asList(end));
		newFixedThreadPool.shutdown();

		// Ensure we receive AT command as well
		Reply<?> next = collector.getData().get(new CustomATCommand("Z")).iterator().next();
		Assertions.assertThat(next).isNotNull();

		PidRegistry pidRegistry = workflow.getPidRegistry();
		StatisticsRegistry statisticsRegistry = workflow.getStatisticsRegistry();

		final PidDefinition p1 = pidRegistry.findBy(6l);// Engine coolant temperature
		final double rate1 = statisticsRegistry.getRatePerSec(p1);
		final PidDefinition p2 = pidRegistry.findBy(13l);// Engine RPM
		final double rate2 = statisticsRegistry.getRatePerSec(p2);

		log.info("Pid: {}, rate: {}", p1.getDescription(), rate1);
		log.info("Pid: {}, rate: {}", p2.getDescription(), rate2);

		Assertions.assertThat(rate1).isGreaterThan(0);
		Assertions.assertThat(rate2).isGreaterThan(0);

		
		//coolant should have less RPS than RPM
		Assertions.assertThat(rate1).isLessThanOrEqualTo(rate2);
	}
}
