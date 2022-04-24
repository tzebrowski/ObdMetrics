package org.obd.metrics.api.cache;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.obd.metrics.Reply;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.api.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.Adjustments;
import org.obd.metrics.api.CacheConfig;
import org.obd.metrics.api.InitConfiguration;
import org.obd.metrics.api.Pids;
import org.obd.metrics.api.ProducerPolicy;
import org.obd.metrics.api.Query;
import org.obd.metrics.api.Workflow;
import org.obd.metrics.api.WorkflowFinalizer;
import org.obd.metrics.connection.SmartMockConnection;
import org.obd.metrics.diagnostic.RateType;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.AdapterConnection;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FormulaCacheTest {
	@Disabled
	@Test
	public void longRunningTest() throws IOException, InterruptedException, ExecutionException {

		final Query query = Query.builder()
		        .pid(6l) // Engine coolant temperature
		        .pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        .pid(16l) // Intake air temperature
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .pid(15l) // Timing advance
		        .pid(9000l) // Battery voltage
		        .build();

		final AdapterConnection connection = SmartMockConnection.builder()
		        .query(query)
		        .numberOfEntries(128 * 128 * 128 * 4).build();

		int commandFrequency = 6;
		final Workflow workflow = Workflow
		        .instance()
		        .observer(new ReplyObserver<Reply<?>>() {

			        @Override
			        public void onNext(Reply<?> t) {
				        log.trace("{}", t);
			        }
		        })
		        .init(InitConfiguration.DEFAULT)
		        .pids(Pids.DEFAULT)
		        .initialize();

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
		                .checkInterval(5000)
		                .commandFrequency(commandFrequency)
		                .build())
		        .producerPolicy(ProducerPolicy.builder()
		                .priorityQueueEnabled(Boolean.TRUE)
		                .lowPriorityCommandFrequencyDelay(2000).build())
		        .batchEnabled(true)
		        .build();

		workflow.start(connection, query, optional);

		WorkflowFinalizer.finalizeAfter(workflow, TimeUnit.MINUTES.toMillis(1), () -> false);

		final PidDefinitionRegistry rpm = workflow.getPidRegistry();

		PidDefinition measuredPID = rpm.findBy(13l);
		double ratePerSec = workflow.getDiagnostics().rate().findBy(RateType.MEAN, measuredPID).get().getValue();
		log.info("Rate:{}  ->  {}", measuredPID, ratePerSec);
		Thread.sleep(15000); // wait for saving activity
	}
}
