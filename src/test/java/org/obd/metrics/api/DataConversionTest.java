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
import org.obd.metrics.ObdMetric;
import org.obd.metrics.command.group.AlfaMed17CommandGroup;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinition;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataConversionTest {
	
	@Test
	public void typesTest() throws IOException, InterruptedException  {
		
		DummyObserver observer = new DummyObserver();
		final Workflow workflow = WorkflowFactory.generic()
				.ecuSpecific(EcuSpecific
					.builder()
					.initSequence(AlfaMed17CommandGroup.CAN_INIT_NO_DELAY)
					.pidFile("alfa.json").build())
				.observer(observer)
				.build();
		
		workflow.getPids().register(new PidDefinition(10001l, 2, "((A *256 ) +B)/4", "22", "2000", "C", "Engine RPM V2","0", "100",PidDefinition.Type.INT));
		workflow.getPids().register(new PidDefinition(10002l, 2, "((A *256 ) +B)/4", "22", "2002", "C", "Engine RPM V2","0", "100",PidDefinition.Type.SHORT));
		workflow.getPids().register(new PidDefinition(10003l, 2, "((A *256 ) +B)/4", "22", "2004", "C", "Engine RPM V2","0", "100",PidDefinition.Type.DOUBLE));

		
		final Set<Long> ids = new HashSet<>();
		ids.add(10001l); // engine RPM V2
		ids.add(10002l);
		ids.add(10003l);
		
		final MockConnection connection = MockConnection.builder()
						.commandReply("222000", "6220000BEA")
						.commandReply("222002", "6220020BEA")
						.commandReply("222004", "6220040BEA")
						.build();
		
		
		workflow.connection(connection).filter(ids).batch(false).start();
		final Callable<String> end = () -> {
			Thread.sleep(1 * 1500);
			log.info("Ending the process of collecting the data");
			workflow.stop();
			return "end";
		};

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(1);
		newFixedThreadPool.invokeAll(Arrays.asList(end));
				
		newFixedThreadPool.shutdown();

		ObdMetric next = (ObdMetric) observer.getData().get(new ObdCommand(workflow.getPids().findBy(10002l))).iterator().next();
		Assertions.assertThat(next.getValue()).isInstanceOf(Short.class);
		
		
		next = (ObdMetric) observer.getData().get(new ObdCommand(workflow.getPids().findBy(10001l))).iterator().next();
		Assertions.assertThat(next.getValue()).isInstanceOf(Integer.class);
		
		next = (ObdMetric) observer.getData().get(new ObdCommand(workflow.getPids().findBy(10003l))).iterator().next();
		Assertions.assertThat(next.getValue()).isInstanceOf(Double.class);
		
	}
	
	@Test
	public void wrongFormulaTest() throws IOException, InterruptedException  {
		final DummyObserver observer = new DummyObserver();
		final Workflow workflow = WorkflowFactory.generic()
				.ecuSpecific(EcuSpecific
					.builder()
					.initSequence(AlfaMed17CommandGroup.CAN_INIT_NO_DELAY)
					.pidFile("alfa.json").build())
				.observer(new DummyObserver())
				.build();
		
		long id = 10001l;
		workflow.getPids().register(new PidDefinition(id, 2, "(A *256 ) +B )/4", "22", "2000", "C", "Engine RPM V2","0", "100",PidDefinition.Type.DOUBLE));
		
		final Set<Long> ids = new HashSet<>();
		ids.add(id); // engine RPM V2
		
		final MockConnection connection = MockConnection.builder()
						.commandReply("222000", "6220000BEA")
						.build();
		
		
		workflow.connection(connection).filter(ids).batch(false).start();
		final Callable<String> end = () -> {
			Thread.sleep(1 * 1500);
			log.info("Ending the process of collecting the data");
			workflow.stop();
			return "end";
		};

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(1);
		newFixedThreadPool.invokeAll(Arrays.asList(end));
				
		newFixedThreadPool.shutdown();
		
//		ObdMetric next = (ObdMetric) observer.getData().get(new ObdCommand(workflow.getPids().findBy(id))).iterator().next();
//		Assertions.assertThat(next.getValue()).isNull();
		
	
	}
	
	
	@Test
	public void noFormulaTest() throws IOException, InterruptedException  {
		final DummyObserver observer = new DummyObserver();
		
		final Workflow workflow = WorkflowFactory.generic()
				.ecuSpecific(EcuSpecific
					.builder()
					.initSequence(AlfaMed17CommandGroup.CAN_INIT_NO_DELAY)
					.pidFile("alfa.json").build())
				.observer(new DummyObserver())
				.build();
		
		long id = 10001l;
		workflow.getPids().register(new PidDefinition(id, 2, "", "22", "2000", "C", "Engine RPM V2","0", "100",PidDefinition.Type.DOUBLE));
		
		final Set<Long> ids = new HashSet<>();
		ids.add(id); // engine RPM V2
		
		final MockConnection connection = MockConnection.builder()
						.commandReply("222000", "6220000BEA")
						.build();
		
		
		workflow.connection(connection).filter(ids).batch(false).start();
		final Callable<String> end = () -> {
			Thread.sleep(1 * 1500);
			log.info("Ending the process of collecting the data");
			workflow.stop();
			return "end";
		};

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(1);
		newFixedThreadPool.invokeAll(Arrays.asList(end));
				
		newFixedThreadPool.shutdown();

//		ObdMetric next = (ObdMetric) observer.getData().get(new ObdCommand(workflow.getPids().findBy(id))).iterator().next();
//		Assertions.assertThat(next.getValue()).isNull();
	
	}
	
}
