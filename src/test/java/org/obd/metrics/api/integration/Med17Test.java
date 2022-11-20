package org.obd.metrics.api.integration;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;
import org.obd.metrics.PIDsRegistry;
import org.obd.metrics.PIDsRegistryFactory;
import org.obd.metrics.api.Workflow;
import org.obd.metrics.api.WorkflowFinalizer;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Init.Header;
import org.obd.metrics.api.model.Init.Protocol;
import org.obd.metrics.api.model.Pids;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.api.model.STNxxExtensions;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.connection.BluetoothConnection;
import org.obd.metrics.diagnostic.RateType;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.AdapterConnection;
import org.obd.metrics.transport.TcpAdapterConnection;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Med17Test {
	// [01, 03, 04, 05, 06, 07, 0b, 0c, 0d, 0e, 0f, 10, 11, 13, 15, 1c],
	// raw=4100be3fa811]
	
	@Test
	public void stnTest() throws IOException, InterruptedException, ExecutionException {
		final Logger logger = (Logger) LoggerFactory.getLogger("org.obd.metrics.transport.DefaultConnector");
		logger.setLevel(Level.TRACE);
		
		final AdapterConnection connection = TcpAdapterConnection.of("192.168.0.10", 35000);

		final DataCollector collector = new DataCollector();

		int commandFrequency = 6;
		final Workflow workflow = Workflow
		        .instance()
		        .pids(Pids.DEFAULT)
		        .observer(collector)
		        .initialize();
		
		final PIDsRegistry registry = PIDsRegistryFactory.get("mode01.json");
		final Query query = Query.builder()
		        .pid(registry.findBy("15").getId())
		        .pid(registry.findBy("0D").getId())
		        .pid(registry.findBy("0E").getId())
		        .pid(registry.findBy("0B").getId())
		        .pid(registry.findBy("0C").getId()) 
		        .pid(registry.findBy("04").getId()) 
		        .pid(registry.findBy("0F").getId())
		        .pid(registry.findBy("05").getId())
		        .build();

		final Adjustments optional = Adjustments
		        .builder()
		        .stNxx(STNxxExtensions.builder().enabled(Boolean.TRUE).build())
		        .responseLengthEnabled(Boolean.FALSE)
		        .vehicleCapabilitiesReadingEnabled(Boolean.TRUE)
		        .adaptiveTiming(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.TRUE)
		                .checkInterval(1)
		                .commandFrequency(commandFrequency)
		                .build())
		        .producerPolicy(ProducerPolicy.builder()
		                .priorityQueueEnabled(Boolean.TRUE)
		                .build())
		        .cacheConfig(CachePolicy.builder().resultCacheEnabled(false).build())
		        .batchEnabled(true)
		        .build();

		 final Init init = Init.builder()
			.header(Header.builder().header("7DF").mode("01").build())   
            .delay(0)
	        .protocol(Protocol.CAN_11)
	        .sequence(DefaultCommandGroup.INIT)
	        .build();
		 
		workflow.start(connection, query, init, optional);

		WorkflowFinalizer.finalizeAfter(workflow, 25000, () -> false);

		final PidDefinitionRegistry rpm = workflow.getPidRegistry();

		PidDefinition measuredPID = rpm.findBy(13l);
		double ratePerSec = workflow.getDiagnostics().rate().findBy(RateType.MEAN, measuredPID).get().getValue();

		log.info("Rate:{}  ->  {}", measuredPID.getPid(), ratePerSec);

		Assertions.assertThat(ratePerSec).isGreaterThanOrEqualTo(commandFrequency);
	}
	
	@Test
	public void tcpConnection() throws IOException, InterruptedException, ExecutionException {
		final AdapterConnection connection = BluetoothConnection.openConnection();
		final DataCollector collector = new DataCollector();

		int commandFrequency = 6;
		final Workflow workflow = Workflow
		        .instance()
		        .pids(Pids.DEFAULT)
		        .observer(collector)
		        .initialize();

		final PIDsRegistry registry = PIDsRegistryFactory.get("mode01.json");

		final Query query = Query.builder()
		        .pid(registry.findBy("15").getId()) 
		        .pid(registry.findBy("0B").getId())
		        .pid(registry.findBy("0C").getId()) 
		        .pid(registry.findBy("04").getId()) 
		        .pid(registry.findBy("11").getId())
		        .pid(registry.findBy("0E").getId())
		        .pid(registry.findBy("0F").getId())
		        .pid(registry.findBy("05").getId())
		        
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
		                .build())
		        .batchEnabled(true)
		        .build();

		workflow.start(connection, query, Init.DEFAULT, optional);

		WorkflowFinalizer.finalizeAfter(workflow, 270000, () -> false);

		final PidDefinitionRegistry rpm = workflow.getPidRegistry();

		PidDefinition measuredPID = rpm.findBy(13l);
		double ratePerSec = workflow.getDiagnostics().rate().findBy(RateType.MEAN, measuredPID).get().getValue();

		log.info("Rate:{}  ->  {}", measuredPID, ratePerSec);

		Assertions.assertThat(ratePerSec).isGreaterThanOrEqualTo(commandFrequency);
	}
}
