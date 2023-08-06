package org.obd.metrics.api.load;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.Workflow;
import org.obd.metrics.api.WorkflowFinalizer;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Init.Header;
import org.obd.metrics.api.model.Init.Protocol;
import org.obd.metrics.api.model.Pids;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.connection.BluetoothConnection;
import org.obd.metrics.diagnostic.Diagnostics;
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
		        .resource(Thread.currentThread().getContextClassLoader().getResource("mode01_3.json"))
		        .resource(Thread.currentThread().getContextClassLoader().getResource("alfa.json")).build();
		
		final Init init = Init.builder()
		        .delayAfterInit(1000)
		        .header(Header.builder().mode("22").header("DA10F1").build())
				.header(Header.builder().mode("01").header("7DF").build())
		        .protocol(Protocol.CAN_11)
		        .sequence(DefaultCommandGroup.INIT).build();
		
		final Workflow workflow = Workflow
		        .instance()
		        .pids(pids)
		        .observer(new ReplyObserver<Reply<?>>() {
			        @Override
			        public void onNext(Reply<?> t) {
//				        log.info("{}", t);
			        }
		        })
		        .initialize();

		final Query query = Query.builder()
					.pid(13l)     // Engine RPM
			        .pid(12l)    // Boost
			        .pid(18l)   // Throttle position
			        .pid(14l)  // Vehicle speed
			        .pid(5l)  //  Engine load
					.pid(7l) // Short fuel trim
			        .build();

		final Adjustments optional = Adjustments
		        .builder()
		        .collectRawConnectorResponseEnabled(Boolean.FALSE)
		        .cachePolicy(
		                CachePolicy.builder()
		                        .storeResultCacheOnDisk(Boolean.FALSE)
		                        .resultCacheFilePath("./result_cache.json")
		                        .resultCacheEnabled(Boolean.FALSE).build())
		        .adaptiveTimeoutPolicy(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.TRUE)
		                .checkInterval(2000)
		                .commandFrequency(10)
		                .build())
		        .producerPolicy(ProducerPolicy.builder()
		                .priorityQueueEnabled(Boolean.TRUE)
		                .conditionalSleepEnabled(Boolean.FALSE)
		                .build())
		        .batchPolicy(BatchPolicy.builder().enabled(Boolean.TRUE).build())
		        .build();

		workflow.start(connection, query, init, optional);

		WorkflowFinalizer.finalizeAfter(workflow, TimeUnit.MINUTES.toMillis(65), () -> false);

		final PidDefinitionRegistry pidRegistry = workflow.getPidRegistry();
		final PidDefinition rpm = pidRegistry.findBy(13l);
		final Diagnostics diagnostics = workflow.getDiagnostics();
		final double ratePerSec = diagnostics.rate().findBy(RateType.MEAN, rpm).get().getValue();

		log.info("Rate:{}  ->  {}", rpm, ratePerSec);

		Assertions.assertThat(ratePerSec).isGreaterThanOrEqualTo(8);
		
	}
}
