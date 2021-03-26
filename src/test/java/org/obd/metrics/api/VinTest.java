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
import org.obd.metrics.DeviceProperties;
import org.obd.metrics.Lifecycle;
import org.obd.metrics.ObdMetric;
import org.obd.metrics.Reply;
import org.obd.metrics.command.at.CustomATCommand;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VinTest {

	static class LifecycleImpl implements Lifecycle {

		@Getter
		DeviceProperties properties;

		@Override
		public void onRunning(DeviceProperties props) {
			log.info("Device properties {}", props.getProperties());
			this.properties = props;
		}
	}

	@Test
	public void correctTest() throws IOException, InterruptedException {
		final LifecycleImpl lifecycle = new LifecycleImpl();
		final DataCollector collector = new DataCollector();

		final Workflow workflow = SimpleWorkflowFactory.getMode01Workflow(lifecycle, collector);

		final Set<Long> ids = new HashSet<>();
		ids.add(6l); // Engine coolant temperature
		ids.add(12l); // Intake manifold absolute pressure
		ids.add(13l); // Engine RPM
		ids.add(16l); // Intake air temperature
		ids.add(18l); // Throttle position
		ids.add(14l); // Vehicle speed

		final MockConnection connection = MockConnection.builder()
		        .commandReply("09 02", "SEARCHING...0140:4902015756571:5A5A5A314B5A412:4D363930333932")
		        .commandReply("0100", "4100be3ea813")
		        .commandReply("0200", "4140fed00400")
		        .commandReply("0105", "410522")
		        .commandReply("010C", "410c541B")
		        .commandReply("010B", "410b35")
		        .build();

		workflow.start(connection, Adjustements
		        .builder()
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

		// Ensure we receive AT command as well
		Reply<?> next = collector.getData().get(new CustomATCommand("Z")).iterator().next();
		Assertions.assertThat(next).isNotNull();

		final List<ObdMetric> collection = collector.findMetricsBy(workflow.getPidRegistry().findBy(6l));
		Assertions.assertThat(collection.isEmpty()).isFalse();
		
		final ObdMetric metric = collection.iterator().next();
		Assertions.assertThat(metric.getValue()).isInstanceOf(Integer.class);
		Assertions.assertThat(metric.getValue()).isEqualTo(-6);
		Assertions.assertThat(metric.valueToDouble()).isEqualTo(-6.0);
		Assertions.assertThat(metric.valueToString()).isEqualTo("-6");
		
		Assertions.assertThat(lifecycle.properties.getProperties()).containsEntry("VIN", "WVWZZZ1KZAM690392");
	}

	@Test
	public void incorrectTest() throws IOException, InterruptedException {
		final LifecycleImpl lifecycle = new LifecycleImpl();

		final DataCollector collector = new DataCollector();
		final Workflow workflow = SimpleWorkflowFactory.getMode01Workflow(lifecycle, collector);

		final Set<Long> ids = new HashSet<>();
		ids.add(6l); // Engine coolant temperature
		ids.add(12l); // Intake manifold absolute pressure
		ids.add(13l); // Engine RPM
		ids.add(16l); // Intake air temperature
		ids.add(18l); // Throttle position
		ids.add(14l); // Vehicle speed

		final String vinMessage = "0140:4802015756571:5a5a5a314b5a412:4d363930333932";
		final MockConnection connection = MockConnection.builder()
		        .commandReply("09 02", vinMessage)
		        .commandReply("0100", "4100be3ea813")
		        .commandReply("0200", "4140fed00400")
		        .commandReply("0105", "410522")
		        .commandReply("010C", "410c541B")
		        .commandReply("010B", "410b35")
		        .build();

		workflow.start(connection, Adjustements
		        .builder()
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

		// Ensure we receive AT command as well
		Reply<?> next = collector.getData().get(new CustomATCommand("Z")).iterator().next();
		Assertions.assertThat(next).isNotNull();

		final List<ObdMetric> collection = collector.findMetricsBy(workflow.getPidRegistry().findBy(6l));
		Assertions.assertThat(collection.isEmpty()).isFalse();
		
		final ObdMetric metric = collection.iterator().next();
		Assertions.assertThat(metric.getValue()).isInstanceOf(Integer.class);
		Assertions.assertThat(metric.getValue()).isEqualTo(-6);
		Assertions.assertThat(metric.valueToDouble()).isEqualTo(-6.0);
		Assertions.assertThat(metric.valueToString()).isEqualTo("-6");

		// failed decoding VIN
		Assertions.assertThat(lifecycle.properties.getProperties()).containsEntry("VIN", vinMessage);
	}
}
