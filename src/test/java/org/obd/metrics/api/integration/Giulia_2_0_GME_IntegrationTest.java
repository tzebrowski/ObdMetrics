package org.obd.metrics.api.integration;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;
import org.obd.metrics.api.Workflow;
import org.obd.metrics.api.WorkflowFinalizer;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.CacheConfig;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Init.Header;
import org.obd.metrics.api.model.Init.Protocol;
import org.obd.metrics.api.model.Pids;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.connection.BluetoothConnection;
import org.obd.metrics.diagnostic.RateType;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.AdapterConnection;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Giulia_2_0_GME_IntegrationTest {

	
	
	@Test
	public void case_0() throws IOException, InterruptedException, ExecutionException {
		final AdapterConnection connection = BluetoothConnection.of("AABBCC112233"); 
		final DataCollector collector = new DataCollector(true);

		final Pids pids = Pids
		        .builder()
		        .resource(Thread.currentThread().getContextClassLoader().getResource("giulia_2.0_gme.json"))
		        .resource(Thread.currentThread().getContextClassLoader().getResource("extra.json"))
		        .resource(Thread.currentThread().getContextClassLoader().getResource("mode01.json"))
		        .resource(Thread.currentThread().getContextClassLoader().getResource("alfa.json")).build();
		
		int commandFrequency = 6;
		final Workflow workflow = Workflow
		        .instance()
		        .pids(pids)
		        .observer(collector)
		        .initialize();

		final Query query = Query.builder()
				.pid(7005l) 
				.pid(7006l) 
		        .pid(7007l) 
		        .pid(7008l) 
				.build();

		final Adjustments optional = Adjustments
		        .builder()
		        .adaptiveTiming(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.TRUE)
		                .checkInterval(5000)
		                .commandFrequency(commandFrequency)
		                .build())
		        .producerPolicy(ProducerPolicy.builder()
		                .priorityQueueEnabled(Boolean.TRUE)
		                .lowPriorityCommandFrequencyDelay(2000).build())
		        .cacheConfig(CacheConfig.builder().resultCacheEnabled(Boolean.FALSE).build())
		        .batchEnabled(Boolean.TRUE)
		        .build();

		final Init init = Init.builder()
		        .delay(1000)
		        .header(Header.builder().mode("22").header("DA10F1").build())
				.header(Header.builder().mode("01").header("DB33F1").build())
		        .protocol(Protocol.CAN_29)
		        .fetchDeviceProperties(Boolean.FALSE)
		        .fetchSupportedPids(Boolean.FALSE)	
		        .sequence(DefaultCommandGroup.INIT).build();
		
		workflow.start(connection, query, init, optional);

		WorkflowFinalizer.finalizeAfter(workflow, 10000, () -> false);

		final PidDefinitionRegistry rpm = workflow.getPidRegistry();

		PidDefinition measuredPID = rpm.findBy(13l);
		double ratePerSec = workflow.getDiagnostics().rate().findBy(RateType.MEAN, measuredPID).get().getValue();

		log.info("Rate:{}  ->  {}", measuredPID, ratePerSec);

		Assertions.assertThat(ratePerSec).isGreaterThanOrEqualTo(commandFrequency);
	}
	
	
	@Test
	public void case_1() throws IOException, InterruptedException, ExecutionException {
		final AdapterConnection connection = BluetoothConnection.of("AABBCC112233"); 
		final DataCollector collector = new DataCollector(true);

		final Pids pids = Pids
		        .builder()
		        .resource(Thread.currentThread().getContextClassLoader().getResource("giulia_2.0_gme.json"))
		        .resource(Thread.currentThread().getContextClassLoader().getResource("extra.json"))
		        .resource(Thread.currentThread().getContextClassLoader().getResource("mode01.json"))
		        .resource(Thread.currentThread().getContextClassLoader().getResource("alfa.json")).build();
		
		int commandFrequency = 6;
		final Workflow workflow = Workflow
		        .instance()
		        .pids(pids)
		        .observer(collector)
		        .initialize();

		final Query query = Query.builder()
				.pid(7001l) 
				.pid(7002l) 
		        .pid(7003l) 
		        .pid(7004l) 
				.build();

		final Adjustments optional = Adjustments
		        .builder()
		        .adaptiveTiming(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.TRUE)
		                .checkInterval(5000)
		                .commandFrequency(commandFrequency)
		                .build())
		        .producerPolicy(ProducerPolicy.builder()
		                .priorityQueueEnabled(Boolean.TRUE)
		                .lowPriorityCommandFrequencyDelay(2000).build())
		        .cacheConfig(CacheConfig.builder().resultCacheEnabled(Boolean.FALSE).build())
		        .batchEnabled(Boolean.TRUE)
		        .build();

		final Init init = Init.builder()
		        .delay(1000)
		        .header(Header.builder().mode("22").header("DA10F1").build())
				.header(Header.builder().mode("01").header("DB33F1").build())
		        .protocol(Protocol.CAN_29)
		        .fetchDeviceProperties(Boolean.FALSE)
		        .fetchSupportedPids(Boolean.FALSE)	
		        .sequence(DefaultCommandGroup.INIT).build();
		
		workflow.start(connection, query, init, optional);

		WorkflowFinalizer.finalizeAfter(workflow, 10000, () -> false);

		final PidDefinitionRegistry rpm = workflow.getPidRegistry();

		PidDefinition measuredPID = rpm.findBy(13l);
		double ratePerSec = workflow.getDiagnostics().rate().findBy(RateType.MEAN, measuredPID).get().getValue();

		log.info("Rate:{}  ->  {}", measuredPID, ratePerSec);

		Assertions.assertThat(ratePerSec).isGreaterThanOrEqualTo(commandFrequency);
	}
	
	
	@Test
	public void case_2() throws IOException, InterruptedException, ExecutionException {
		final AdapterConnection connection = BluetoothConnection.of("AABBCC112233"); 
		final DataCollector collector = new DataCollector(true);

		final Pids pids = Pids
		        .builder()
		        .resource(Thread.currentThread().getContextClassLoader().getResource("giulia_2.0_gme.json"))
		        .resource(Thread.currentThread().getContextClassLoader().getResource("extra.json"))
		        .resource(Thread.currentThread().getContextClassLoader().getResource("mode01.json"))
		        .resource(Thread.currentThread().getContextClassLoader().getResource("alfa.json")).build();
		
		int commandFrequency = 6;
		final Workflow workflow = Workflow
		        .instance()
		        .pids(pids)
		        .observer(collector)
		        .initialize();

		final Query query = Query.builder()
				.pid(6005l) 
				.pid(6007l) 
		        .pid(6008l) 
		        .pid(6009l)
		        .pid(6010l)
		        .pid(6011l)
		        .pid(6012l)
		        .pid(6013l)
		        .pid(6014l)
				.build();

		final Adjustments optional = Adjustments
		        .builder()
		        .adaptiveTiming(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.TRUE)
		                .checkInterval(5000)
		                .commandFrequency(commandFrequency)
		                .build())
		        .producerPolicy(ProducerPolicy.builder()
		                .priorityQueueEnabled(Boolean.TRUE)
		                .lowPriorityCommandFrequencyDelay(2000).build())
		        .cacheConfig(CacheConfig.builder().resultCacheEnabled(Boolean.FALSE).build())
		        .batchEnabled(Boolean.TRUE)
		        .build();

		final Init init = Init.builder()
		        .delay(1000)
		        .header(Header.builder().mode("22").header("DA10F1").build())
				.header(Header.builder().mode("01").header("DB33F1").build())
		        .protocol(Protocol.CAN_29)
		        .fetchDeviceProperties(Boolean.FALSE)
		        .fetchSupportedPids(Boolean.FALSE)	
		        .sequence(DefaultCommandGroup.INIT).build();
		
		workflow.start(connection, query, init, optional);

		WorkflowFinalizer.finalizeAfter(workflow, 10000, () -> false);

		final PidDefinitionRegistry rpm = workflow.getPidRegistry();

		PidDefinition measuredPID = rpm.findBy(13l);
		double ratePerSec = workflow.getDiagnostics().rate().findBy(RateType.MEAN, measuredPID).get().getValue();

		log.info("Rate:{}  ->  {}", measuredPID, ratePerSec);

		Assertions.assertThat(ratePerSec).isGreaterThanOrEqualTo(commandFrequency);
	}

	
	
	@Test
	public void case_3() throws IOException, InterruptedException, ExecutionException {
		final AdapterConnection connection = BluetoothConnection.of("AABBCC112233"); 
		final DataCollector collector = new DataCollector(true);

		final Pids pids = Pids
		        .builder()
		        .resource(Thread.currentThread().getContextClassLoader().getResource("giulia_2.0_gme.json"))
		        .resource(Thread.currentThread().getContextClassLoader().getResource("extra.json"))
		        .resource(Thread.currentThread().getContextClassLoader().getResource("mode01.json"))
		        .resource(Thread.currentThread().getContextClassLoader().getResource("alfa.json")).build();
		
		int commandFrequency = 6;
		final Workflow workflow = Workflow
		        .instance()
		        .pids(pids)
		        .observer(collector)
		        .initialize();

		final Query query = Query.builder()
				.pid(7009l) 
				.pid(6l) 
				.build();

		final Adjustments optional = Adjustments
		        .builder()
		        .adaptiveTiming(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.TRUE)
		                .checkInterval(5000)
		                .commandFrequency(commandFrequency)
		                .build())
		        .producerPolicy(ProducerPolicy.builder()
		                .priorityQueueEnabled(Boolean.TRUE)
		                .lowPriorityCommandFrequencyDelay(2000).build())
		        .cacheConfig(CacheConfig.builder().resultCacheEnabled(Boolean.FALSE).build())
		        .batchEnabled(Boolean.TRUE)
		        .build();

		final Init init = Init.builder()
		        .delay(1000)
		        .header(Header.builder().mode("22").header("DA10F1").build())
				.header(Header.builder().mode("01").header("DB33F1").build())
		        .protocol(Protocol.CAN_29)
		        .fetchDeviceProperties(Boolean.FALSE)
		        .fetchSupportedPids(Boolean.FALSE)	
		        .sequence(DefaultCommandGroup.INIT).build();
		
		workflow.start(connection, query, init, optional);

		WorkflowFinalizer.finalizeAfter(workflow, 10000, () -> false);

		final PidDefinitionRegistry rpm = workflow.getPidRegistry();

		PidDefinition measuredPID = rpm.findBy(13l);
		double ratePerSec = workflow.getDiagnostics().rate().findBy(RateType.MEAN, measuredPID).get().getValue();

		log.info("Rate:{}  ->  {}", measuredPID, ratePerSec);

		Assertions.assertThat(ratePerSec).isGreaterThanOrEqualTo(commandFrequency);
	}
	
	@Test
	public void case_4() throws IOException, InterruptedException, ExecutionException {
		final AdapterConnection connection = BluetoothConnection.of("AABBCC112233"); 
		final DataCollector collector = new DataCollector(true);

		final Pids pids = Pids
		        .builder()
		        .resource(Thread.currentThread().getContextClassLoader().getResource("giulia_2.0_gme.json"))
		        .resource(Thread.currentThread().getContextClassLoader().getResource("extra.json"))
		        .resource(Thread.currentThread().getContextClassLoader().getResource("mode01.json"))
		        .resource(Thread.currentThread().getContextClassLoader().getResource("alfa.json")).build();
		
		int commandFrequency = 6;
		final Workflow workflow = Workflow
		        .instance()
		        .pids(pids)
		        .observer(collector)
		        .initialize();

		final Query query = Query.builder()
				.pid(7010l) 
				.pid(7011l) 
				.build();

		final Adjustments optional = Adjustments
		        .builder()
		        .adaptiveTiming(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.TRUE)
		                .checkInterval(5000)
		                .commandFrequency(commandFrequency)
		                .build())
		        .producerPolicy(ProducerPolicy.builder()
		                .priorityQueueEnabled(Boolean.TRUE)
		                .lowPriorityCommandFrequencyDelay(2000).build())
		        .cacheConfig(CacheConfig.builder().resultCacheEnabled(Boolean.FALSE).build())
		        .batchEnabled(Boolean.TRUE)
		        .build();

		final Init init = Init.builder()
		        .delay(1000)
		        .header(Header.builder().mode("22").header("DA10F1").build())
				.header(Header.builder().mode("01").header("DB33F1").build())
		        .protocol(Protocol.CAN_29)
		        .fetchDeviceProperties(Boolean.FALSE)
		        .fetchSupportedPids(Boolean.FALSE)	
		        .sequence(DefaultCommandGroup.INIT).build();
		
		workflow.start(connection, query, init, optional);

		WorkflowFinalizer.finalizeAfter(workflow, 10000, () -> false);

		final PidDefinitionRegistry rpm = workflow.getPidRegistry();

		PidDefinition measuredPID = rpm.findBy(13l);
		double ratePerSec = workflow.getDiagnostics().rate().findBy(RateType.MEAN, measuredPID).get().getValue();

		log.info("Rate:{}  ->  {}", measuredPID, ratePerSec);

		Assertions.assertThat(ratePerSec).isGreaterThanOrEqualTo(commandFrequency);
	}

}
