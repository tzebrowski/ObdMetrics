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
import org.obd.metrics.command.group.AlfaMed17CommandGroup;
import org.obd.metrics.command.group.Mode1CommandGroup;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidRegistry;
import org.obd.metrics.statistics.Statistics;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StatisticsTest {
	
	@Test
	public void mode01WorkflowTest() throws IOException, InterruptedException{
		
		final DataCollector collector = new DataCollector();
		final Workflow workflow = WorkflowFactory.mode1()
				.ecuSpecific(EcuSpecific
						.builder()
						.initSequence(Mode1CommandGroup.INIT_NO_DELAY)
						.pidFile("mode01.json").build())
				.observer(collector)
				.commandFrequency(0l)
				.initialize();
		
		final Set<Long> ids = new HashSet<>();
		ids.add(6l);  // Engine coolant temperature
		ids.add(12l); // Intake manifold absolute pressure
		ids.add(13l); // Engine RPM
		ids.add(16l); // Intake air temperature
		ids.add(18l); // Throttle position
		ids.add(14l); // Vehicle speed

		final MockConnection connection = MockConnection.builder()
				.commandReply("0100","4100be3ea813")
				.commandReply("0200","4140fed00400")
				.commandReply("01 0B 0C 0D 0F 11 05", "00e0:410bff0c00001:0d000f001100052:00aaaaaaaaaaaa").build();
						
		workflow.filter(ids).batch(true).start(connection);
		final Callable<String> end = () -> {
			Thread.sleep(1 * 2000);
			log.info("Ending the process of collecting the data");
			workflow.stop();
			return "end";
		};

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(1);
		newFixedThreadPool.invokeAll(Arrays.asList(end));
		newFixedThreadPool.shutdown();
		
		
		final PidRegistry pids = workflow.getPids();
		
		final PidDefinition engineTemp = pids.findBy(6l);
		Assertions.assertThat(engineTemp.getPid()).isEqualTo("05");

		Assertions.assertThat(workflow.getStatistics().getRatePerSec(engineTemp)).isGreaterThan(10d);
		Assertions.assertThat(workflow.getStatistics().getRatePerSec(pids.findBy(12l))).isGreaterThan(10d);
		
	}
	
	
	@Test
	public void genericWorkflowTest() throws IOException, InterruptedException  {
	
		DataCollector collector = new DataCollector();
		final Workflow workflow = WorkflowFactory.generic()
				.ecuSpecific(EcuSpecific
					.builder()
					.initSequence(AlfaMed17CommandGroup.CAN_INIT_NO_DELAY)
					.pidFile("alfa.json").build())
				.observer(collector)
				.commandFrequency(0l)
				.initialize();
		
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
		
		workflow.filter(ids).start(connection);
		final Callable<String> end = () -> {
			Thread.sleep(1 * 1500);
			log.info("Ending the process of collecting the data");
			workflow.stop();
			return "end";
		};

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(1);
		newFixedThreadPool.invokeAll(Arrays.asList(end));
		newFixedThreadPool.shutdown();

		
		final PidRegistry pids = workflow.getPids();

		PidDefinition pid8l = pids.findBy(8l);
		Statistics stat8l = workflow.getStatistics().findBy(pid8l);
		Assertions.assertThat(stat8l).isNotNull();
		
		PidDefinition pid4l = pids.findBy(4l);
		Statistics stat4L = workflow.getStatistics().findBy(pid4l);
		Assertions.assertThat(stat4L).isNotNull();
		
		Assertions.assertThat(stat4L.getMax()).isEqualTo(762);
		Assertions.assertThat(stat4L.getMin()).isEqualTo(762);
		Assertions.assertThat(stat4L.getMedian()).isEqualTo(762);
		
		Assertions.assertThat(stat8l.getMax()).isEqualTo(-1);
		Assertions.assertThat(stat8l.getMin()).isEqualTo(-1);
		Assertions.assertThat(stat8l.getMedian()).isEqualTo(-1);
		
		Assertions.assertThat(workflow.getStatistics().getRatePerSec(pid8l)).isGreaterThan(10d);
		Assertions.assertThat(workflow.getStatistics().getRatePerSec(pid4l)).isGreaterThan(10d);
	}
	
}
