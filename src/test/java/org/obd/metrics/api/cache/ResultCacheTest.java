package org.obd.metrics.api.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.map.MultiValueMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.obd.metrics.Reply;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.api.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.Adjustments;
import org.obd.metrics.api.CacheConfig;
import org.obd.metrics.api.PidSpec;
import org.obd.metrics.api.ProducerPolicy;
import org.obd.metrics.api.Query;
import org.obd.metrics.api.Workflow;
import org.obd.metrics.api.WorkflowFactory;
import org.obd.metrics.api.WorkflowFinalizer;
import org.obd.metrics.codec.AnswerCodeCodec;
import org.obd.metrics.codec.CodecTest.PidRegistryCache;
import org.obd.metrics.command.group.Mode1CommandGroup;
import org.obd.metrics.connection.AdapterConnection;
import org.obd.metrics.connection.SmartMockConnection;
import org.obd.metrics.diagnostic.RateType;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResultCacheTest {

//	TRACE org.obd.metrics.connection.DefaultConnector - TX: 01 15 15 0C 11 0D
//	TRACE org.obd.metrics.connection.DefaultConnector - TX: 00E0:41155AFF155A1:FF0C000011000D2:00AAAAAAAAAAAA
	
	
//	TRACE org.obd.metrics.connection.DefaultConnector - TX: 01 0B 0C 11 0D
//	TRACE org.obd.metrics.connection.DefaultConnector - TX: 00A0:410BFF0C00001:11000D00AAAAAA
	
	public static void main(String[] args) {

		List<Long> pids = new ArrayList<>();
		pids.add(6l);
		pids.add(13l);

		PidDefinitionRegistry pidRegistry = PidRegistryCache.get("mode01.json");

		MultiValuedMap<Long, String> answers = generateAnswers(pids, pidRegistry);

		System.out.println(answers.get(6l).size());
		System.out.println(answers.get(13l).size());

	}

	private static MultiValuedMap<Long, String> generateAnswers(List<Long> pids, PidDefinitionRegistry pidRegistry) {
		final MultiValuedMap<Long, String> answers = new ArrayListValuedHashMap<>();
		final AnswerCodeCodec answerCodeCodec = new AnswerCodeCodec(false);

		for (long id : pids) {

			final PidDefinition pid = pidRegistry.findBy(id);
			final String successAnswerCode = answerCodeCodec.getSuccessAnswerCode(pid);

			if (pid.getLength() == 1) {

				for (short i = 0; i < 255; i++) {
					answers.put(pid.getId(), successAnswerCode + String.format("%02X", i));
				}

			} else if (pid.getLength() == 2) {
				for (short j = 0; j < 255; j++) {
					for (short i = 0; i < 255; i++) {
						answers.put(pid.getId(),
						        successAnswerCode + String.format("%02X", j) + String.format("%02X", i));
					}
				}
			}
		}
		return answers;
	}

	@Disabled
	@Test
	public void longRunningTest() throws IOException, InterruptedException, ExecutionException {

		final AdapterConnection connection = SmartMockConnection.builder()
		        .pid(6l) // Engine coolant temperature
		        .pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        .pid(16l) // Intake air temperature
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .pid(15l) // Timing advance
		        .build();

		int commandFrequency = 6;
		final Workflow workflow = WorkflowFactory
		        .mode1()
		        .observer(new ReplyObserver<Reply<?>>() {

			        @Override
			        public void onNext(Reply<?> t) {
				        log.info("{}", t);
			        }
		        })
		        .pidSpec(PidSpec
		                .builder()
		                .initSequence(Mode1CommandGroup.INIT)
		                .pidFile(Thread.currentThread().getContextClassLoader().getResource("mode01.json")).build())
		        .initialize();

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

		final Adjustments optional = Adjustments
		        .builder()
		        .initDelay(1000)
		        .cacheConfig(
		                CacheConfig.builder()
		                        .storeResultCacheOnDisk(Boolean.TRUE)
		                        .resultCacheFilePath("./result_cache.ser")
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

		WorkflowFinalizer.finalizeAfter(workflow, TimeUnit.SECONDS.toMillis(20), () -> false);

		final PidDefinitionRegistry rpm = workflow.getPidRegistry();

		PidDefinition measuredPID = rpm.findBy(13l);
		double ratePerSec = workflow.getDiagnostics().rate().findBy(RateType.MEAN, measuredPID).get().getValue();

		log.info("Rate:{}  ->  {}", measuredPID, ratePerSec);

		Assertions.assertThat(ratePerSec).isGreaterThanOrEqualTo(commandFrequency);

	}
}
