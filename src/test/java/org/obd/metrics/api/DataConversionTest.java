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
import org.obd.metrics.ObdMetric;
import org.obd.metrics.command.group.AlfaMed17CommandGroup;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.Urls;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataConversionTest {
	
	@Test
	public void typesConversionTest() throws IOException, InterruptedException  {
		
		final DataCollector collector = new DataCollector();
		final Workflow workflow = WorkflowFactory.generic()
				.pidSpec(PidSpec
					.builder()
					.initSequence(AlfaMed17CommandGroup.CAN_INIT_NO_DELAY)
					.pidFile(Urls.resourceToUrl("alfa.json")).build())
				.observer(collector)
				.commandFrequency(0l)
				.initialize();
		
		workflow.getPids().register(new PidDefinition(10001l, 2, "((A *256 ) +B)/4", "22", "2000","rpm", "Engine RPM","0", "100",PidDefinition.Type.INT));
		workflow.getPids().register(new PidDefinition(10002l, 2, "((A *256 ) +B)/4", "22", "2002","rpm", "Engine RPM","0", "100",PidDefinition.Type.SHORT));
		workflow.getPids().register(new PidDefinition(10003l, 2, "((A *256 ) +B)/4", "22", "2004","rpm", "Engine RPM","0", "100",PidDefinition.Type.DOUBLE));

		
		final Set<Long> ids = new HashSet<>();
		ids.add(10001l); 
		ids.add(10002l);
		ids.add(10003l);
		
		final MockConnection connection = MockConnection.builder()
						.commandReply("222000", "6220000BEA")
						.commandReply("222002", "6220020BEA")
						.commandReply("222004", "6220040BEA")
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

		ObdMetric next = (ObdMetric) collector.getData().get(new ObdCommand(workflow.getPids().findBy(10002l))).iterator().next();
		Assertions.assertThat(next.getValue()).isInstanceOf(Short.class);
		
		
		next = (ObdMetric) collector.getData().get(new ObdCommand(workflow.getPids().findBy(10001l))).iterator().next();
		Assertions.assertThat(next.getValue()).isInstanceOf(Integer.class);
		
		next = (ObdMetric) collector.getData().get(new ObdCommand(workflow.getPids().findBy(10003l))).iterator().next();
		Assertions.assertThat(next.getValue()).isInstanceOf(Double.class);
		
	}
	
	@Test
	public void invalidFormulaTest() throws IOException, InterruptedException  {
		final DataCollector collector = new DataCollector();
		final Workflow workflow = WorkflowFactory.generic()
				.pidSpec(PidSpec
					.builder()
					.initSequence(AlfaMed17CommandGroup.CAN_INIT_NO_DELAY)
					.pidFile(Urls.resourceToUrl("alfa.json")).build())
				.observer(collector)
				.commandFrequency(0l)
				.initialize();
		
		long id = 10001l;
		
		final String invalidFormula = "(A *256 ) +B )/4";
		workflow.getPids().register(new PidDefinition(id, 2, invalidFormula, "22", "2000","rpm", "Engine RPM","0", "100",PidDefinition.Type.DOUBLE));
		
		final Set<Long> ids = new HashSet<>();
		ids.add(id); 
		
		final MockConnection connection = MockConnection.builder()
						.commandReply("222000", "6220000BEA")
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
		
		ObdMetric next = (ObdMetric) collector.getData().get(new ObdCommand(workflow.getPids().findBy(id))).iterator().next();
		Assertions.assertThat(next.getValue()).isNull();
	}
	
	
	@Test
	public void noFormulaTest() throws IOException, InterruptedException  {
		final DataCollector collector = new DataCollector();
		
		final Workflow workflow = WorkflowFactory.generic()
				.pidSpec(PidSpec
					.builder()
					.initSequence(AlfaMed17CommandGroup.CAN_INIT_NO_DELAY)
					.pidFile(Urls.resourceToUrl("alfa.json")).build())
				.observer(collector)
				.commandFrequency(0l)
				.initialize();
		
		long id = 10001l;
		workflow.getPids().register(new PidDefinition(id, 2, "", "22", "2000","rpm", "Engine RPM","0", "100",PidDefinition.Type.DOUBLE));
		
		final Set<Long> ids = new HashSet<>();
		ids.add(id); 
		
		final MockConnection connection = MockConnection.builder()
						.commandReply("222000", "6220000BEA")
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

		ObdMetric next = (ObdMetric) collector.getData()
				.get(new ObdCommand(workflow.getPids().findBy(id))).iterator().next();
		Assertions.assertThat(next.getValue()).isNull();
	
	}
	
	@Test
	public void invalidaDataTest() throws IOException, InterruptedException  {
		final DataCollector collector = new DataCollector();
		
		final Workflow workflow = WorkflowFactory.generic()
				.pidSpec(PidSpec
					.builder()
					.initSequence(AlfaMed17CommandGroup.CAN_INIT_NO_DELAY)
					.pidFile(Urls.resourceToUrl("alfa.json")).build())
				.observer(collector)
				.commandFrequency(0l)
				.initialize();
		
		long id = 10001l;
		workflow.getPids().register(new PidDefinition(id, 2, "(A *256 ) +B )/4", "22", "2000","rpm", "Engine RPM","0", "100",PidDefinition.Type.DOUBLE));
		
		
		final Set<Long> ids = new HashSet<>();
		ids.add(id); // Coolant
		ids.add(8l); // Coolant
		ids.add(4l); // RPM
		ids.add(7l); // Intake temp
		ids.add(15l);// Oil temp
		ids.add(3l); // Spark Advance
		
		final MockConnection connection = MockConnection.builder()
						.commandReply("222000", "xxxxxxxxxxxxxx")
						.commandReply("221000", "")
						.commandReply("221935", "nodata")
						.commandReply("22194f", "stopped")
						.commandReply("221812", "unabletoconnect")
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
		
		
		ObdMetric next = (ObdMetric) collector.getData().get(new ObdCommand(workflow.getPids().findBy(id))).iterator().next();
		Assertions.assertThat(next.getValue()).isNull();
	}
}
