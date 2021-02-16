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
import org.obd.metrics.DummyObserver;
import org.obd.metrics.command.group.Mode1CommandGroup;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Mode01Test {
	
	@Test
	public void nonBatchTest() throws IOException, InterruptedException {
		
		final Workflow workflow = Workflow.mode1().equationEngine("JavaScript")
				.ecuSpecific(EcuSpecific
						.builder()
						.initSequence(Mode1CommandGroup.INIT_NO_DELAY)
						.pidFile("mode01.json").build())
				.observer(new DummyObserver()).build();
		
		
		final Set<Long> ids = new HashSet<>();
		ids.add(6l);  // Engine coolant temperature
		ids.add(12l); // Intake manifold absolute pressure
		ids.add(13l); // Engine RPM
		ids.add(16l); // Intake air temperature
		ids.add(18l); // Throttle position
		ids.add(14l); // Vehicle speed
		
		final MockedConnection connection = MockedConnection.builder()
					.parameter("0100","4100be3ea813")
					.parameter("0200","4140fed00400")
					.parameter("0105", "410522")
					.parameter("010C", "410c541B")
					.parameter("010D", "")
					.parameter("0111", "no data")
					.parameter("010B", "410b35")
					.readTimeout(0)
					.readTimeout(0)
					.build();
			
		workflow.connection(connection).filter(ids).batch(false).start();
		final Callable<String> end = () -> {
			Thread.sleep(1 * 2000);
			log.info("Ending the process of collecting the data");
			workflow.stop();
			return "end";
		};

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(1);
		newFixedThreadPool.invokeAll(Arrays.asList(end));
		
		final PidRegistry pids = workflow.getPids();
		
		final PidDefinition engineTemp = pids.findBy(6l);
		Assertions.assertThat(engineTemp.getPid()).isEqualTo("05");

		double ratePerSec05 = workflow.getStatistics().getRatePerSec(engineTemp);
		double ratePerSec0C = workflow.getStatistics().getRatePerSec(pids.findBy(12l));

		log.info("Rate: 0105: {}/sec", ratePerSec05);
		log.info("Rate: 010C: {}/sec", ratePerSec0C);

		Assertions.assertThat(ratePerSec05).isGreaterThan(10d);
		Assertions.assertThat(ratePerSec0C).isGreaterThan(10d);
		newFixedThreadPool.shutdown();
	}
	
	@Test
	public void batchTest() throws IOException, InterruptedException{
		
		final Workflow workflow = Workflow.mode1().equationEngine("JavaScript")
				.ecuSpecific(EcuSpecific
						.builder()
						.initSequence(Mode1CommandGroup.INIT_NO_DELAY)
						.pidFile("mode01.json").build())
				.observer(new DummyObserver()).build();
		
		final Set<Long> ids = new HashSet<>();
		ids.add(6l);  // Engine coolant temperature
		ids.add(12l); // Intake manifold absolute pressure
		ids.add(13l); // Engine RPM
		ids.add(16l); // Intake air temperature
		ids.add(18l); // Throttle position
		ids.add(14l); // Vehicle speed

		final MockedConnection connection = MockedConnection.builder()
				.parameter("0100","4100be3ea813")
				.parameter("0200","4140fed00400")
				.parameter("01 0B 0C 0D 0F 11 05", "00e0:410bff0c00001:0d000f001100052:00aaaaaaaaaaaa").build();
						
		workflow.connection(connection).filter(ids).batch(true).start();
		final Callable<String> end = () -> {
			Thread.sleep(1 * 2000);
			log.info("Ending the process of collecting the data");
			workflow.stop();
			return "end";
		};

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(1);
		newFixedThreadPool.invokeAll(Arrays.asList(end));
		
		final PidRegistry pids = workflow.getPids();
		
		final PidDefinition engineTemp = pids.findBy(6l);
		Assertions.assertThat(engineTemp.getPid()).isEqualTo("05");

		double ratePerSec05 = workflow.getStatistics().getRatePerSec(engineTemp);
		double ratePerSec0C = workflow.getStatistics().getRatePerSec(pids.findBy(12l));

		log.info("Rate: 0105: {}/sec", ratePerSec05);
		log.info("Rate: 010C: {}/sec", ratePerSec0C);

		Assertions.assertThat(ratePerSec05).isGreaterThan(10d);
		Assertions.assertThat(ratePerSec0C).isGreaterThan(10d);
		newFixedThreadPool.shutdown();
	}
}
