package org.obd.metrics.api.load;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.Reply;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.api.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.Adjustments;
import org.obd.metrics.api.CacheConfig;
import org.obd.metrics.api.Init;
import org.obd.metrics.api.Init.Header;
import org.obd.metrics.api.Init.Protocol;
import org.obd.metrics.api.Pids;
import org.obd.metrics.api.ProducerPolicy;
import org.obd.metrics.api.Query;
import org.obd.metrics.api.Workflow;
import org.obd.metrics.api.WorkflowFinalizer;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.connection.BluetoothConnection;
import org.obd.metrics.diagnostic.Diagnostics;
import org.obd.metrics.diagnostic.Histogram;
import org.obd.metrics.diagnostic.RateType;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.AdapterConnection;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoadTest {
	
	@Test
	public void loadTest() throws IOException, InterruptedException, ExecutionException {
		final AdapterConnection connection = BluetoothConnection.openConnection();
		
		final Pids pids = Pids
		        .builder()
		        .resource(Thread.currentThread().getContextClassLoader().getResource("extra.json"))
		        .resource(Thread.currentThread().getContextClassLoader().getResource("mode01.json"))
		        .resource(Thread.currentThread().getContextClassLoader().getResource("alfa.json")).build();
		
		final Init init = Init.builder()
		        .delay(1000)
		        .header(Header.builder().mode("22").header("DA10F1").build())
				.header(Header.builder().mode("01").header("DB33F1").build())
		        .protocol(Protocol.CAN_29)
		        .sequence(DefaultCommandGroup.INIT).build();
		
		final Workflow workflow = Workflow
		        .instance()
		        .pids(pids)
		        .initialize();

		final Query query = Query.builder()
				.pid(6013l) 
		        .pid(6014l) 
		        .pid(6005l) 
		        
				.pid(13l) // Engine RPM
		        .pid(12l) // Boost
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .pid(5l) //  Engine load
				.pid(7l)  // Short fuel trim
		        .build();

		int commandFrequency = 10;
		final Adjustments optional = Adjustments
		        .builder()
		        .cacheConfig(
		                CacheConfig.builder()
		                        .storeResultCacheOnDisk(Boolean.TRUE)
		                        .resultCacheFilePath("./result_cache.json")
		                        .resultCacheEnabled(Boolean.TRUE).build())
		        .adaptiveTiming(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.TRUE)
		                .checkInterval(2000)
		                .commandFrequency(9)
		                .build())
		        .producerPolicy(ProducerPolicy.builder()
		                .priorityQueueEnabled(Boolean.TRUE)
		                .lowPriorityCommandFrequencyDelay(2000).build())
		        .batchEnabled(true)
		        .build();

		

		workflow.start(connection, query, init, optional);

		WorkflowFinalizer.finalizeAfter(workflow, TimeUnit.SECONDS.toMillis(30), () -> false);

		final PidDefinitionRegistry pidRegistry = workflow.getPidRegistry();
		final PidDefinition rpm = pidRegistry.findBy(13l);
		final Diagnostics diagnostics = workflow.getDiagnostics();
		final Histogram rpmHist = diagnostics.histogram().findBy(rpm);
		Assertions.assertThat(rpmHist.getMin()).isGreaterThan(500);
		

		final double ratePerSec = diagnostics.rate().findBy(RateType.MEAN, rpm).get().getValue();

		log.info("Rate:{}  ->  {}", rpm, ratePerSec);

		Assertions.assertThat(ratePerSec).isGreaterThanOrEqualTo(commandFrequency);
		
	}
}
