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
import org.obd.metrics.DeviceProperties;
import org.obd.metrics.ObdMetric;
import org.obd.metrics.Reply;
import org.obd.metrics.StatusObserver;
import org.obd.metrics.command.at.CustomATCommand;
import org.obd.metrics.command.group.Mode1CommandGroup;
import org.obd.metrics.command.obd.ObdCommand;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VinTest {
	
	static class Notifications implements StatusObserver {

		@Getter
		DeviceProperties properties;

		
		@Override
		public void onConnected(DeviceProperties props) {
			log.info("Device properties {}",props.getProperties());
			this.properties = props;
		}
	}
	
	@Test
	public void correctTest() throws IOException, InterruptedException {
		final Notifications notifications = new Notifications();
		
		final DataCollector collector = new DataCollector();
		final Workflow workflow = WorkflowFactory.mode1()
				.ecuSpecific(EcuSpecific
						.builder()
						.initSequence(Mode1CommandGroup.INIT_NO_DELAY)
						.pidFile("mode01.json").build())
				.commandFrequency(0l)
				.statusObserver(notifications)
				.observer(collector).initialize();
		
		
		final Set<Long> ids = new HashSet<>();
		ids.add(6l);  // Engine coolant temperature
		ids.add(12l); // Intake manifold absolute pressure
		ids.add(13l); // Engine RPM
		ids.add(16l); // Intake air temperature
		ids.add(18l); // Throttle position
		ids.add(14l); // Vehicle speed
		
		final MockConnection connection = MockConnection.builder()
					.commandReply("09 02","0140:4902015756571:5a5a5a314b5a412:4d363930333932")
					.commandReply("0100","4100be3ea813")
					.commandReply("0200","4140fed00400")
					.commandReply("0105", "410522")
					.commandReply("010C", "410c541B")
					.commandReply("010B", "410b35")
					.readTimeout(0)
					.readTimeout(0)
					.build();
			
		workflow.start(WorkflowContext
				.builder()
				.connection(connection)
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
		
		//Ensure we receive AT command as well
		Reply<?> next = collector.getData().get(new CustomATCommand("Z")).iterator().next();
		Assertions.assertThat(next).isNotNull();

		ObdMetric metric = (ObdMetric) collector.getData().get(new ObdCommand(workflow.getPids().findBy(6l))).iterator().next();
		Assertions.assertThat(metric.getValue()).isInstanceOf(Integer.class);
		Assertions.assertThat(metric.getValue()).isEqualTo(-6);
		Assertions.assertThat(metric.valueToDouble()).isEqualTo(-6.0);
		Assertions.assertThat(metric.valueToString()).isEqualTo("-6");
	
		Assertions.assertThat(notifications.properties.getProperties()).containsEntry("VIN", "WVWZZZ1KZAM690392");
	}
	
	
	@Test
	public void incorrectTest() throws IOException, InterruptedException {
		final Notifications notifications = new Notifications();
		
		final DataCollector collector = new DataCollector();
		final Workflow workflow = WorkflowFactory.mode1()
				.ecuSpecific(EcuSpecific
						.builder()
						.initSequence(Mode1CommandGroup.INIT_NO_DELAY)
						.pidFile("mode01.json").build())
				.commandFrequency(0l)
				.statusObserver(notifications)
				.observer(collector).initialize();
		
		
		final Set<Long> ids = new HashSet<>();
		ids.add(6l);  // Engine coolant temperature
		ids.add(12l); // Intake manifold absolute pressure
		ids.add(13l); // Engine RPM
		ids.add(16l); // Intake air temperature
		ids.add(18l); // Throttle position
		ids.add(14l); // Vehicle speed
		
		final MockConnection connection = MockConnection.builder()
					.commandReply("09 02","0140:4802015756571:5a5a5a314b5a412:4d363930333932")
					.commandReply("0100","4100be3ea813")
					.commandReply("0200","4140fed00400")
					.commandReply("0105", "410522")
					.commandReply("010C", "410c541B")
					.commandReply("010B", "410b35")
					.readTimeout(0)
					.readTimeout(0)
					.build();
			
		workflow.start(WorkflowContext
				.builder()
				.connection(connection)
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
		
		//Ensure we receive AT command as well
		Reply<?> next = collector.getData().get(new CustomATCommand("Z")).iterator().next();
		Assertions.assertThat(next).isNotNull();

		ObdMetric metric = (ObdMetric) collector.getData().get(new ObdCommand(workflow.getPids().findBy(6l))).iterator().next();
		Assertions.assertThat(metric.getValue()).isInstanceOf(Integer.class);
		Assertions.assertThat(metric.getValue()).isEqualTo(-6);
		Assertions.assertThat(metric.valueToDouble()).isEqualTo(-6.0);
		Assertions.assertThat(metric.valueToString()).isEqualTo("-6");
	
		Assertions.assertThat(notifications.properties.getProperties()).containsEntry("VIN", "0140:4802015756571:5a5a5a314b5a412:4d363930333932");
	}
}
