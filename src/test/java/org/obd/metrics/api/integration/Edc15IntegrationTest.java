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
import org.obd.metrics.api.model.Init;
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
public class Edc15IntegrationTest {

	@Test
	public void test() throws IOException, InterruptedException, ExecutionException {
		final AdapterConnection connection = BluetoothConnection.of("AABBCC112233"); 
		final DataCollector collector = new DataCollector();

		int commandFrequency = 6;
		final Workflow workflow = Workflow
		        .instance()
		        .pids(Pids.DEFAULT)
		        .observer(collector)
		        .initialize();

		final Query query = Query.builder()
		        .pid(12l) // Engine RPM
		        .pid(13l) // Intake air temperature
		        .build();

		final Adjustments optional = Adjustments
		        .builder()
		        .vehicleCapabilitiesReadingEnabled(Boolean.FALSE)
		        .vehicleMetadataReadingEnabled(Boolean.FALSE)
		        .adaptiveTiming(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.TRUE)
		                .checkInterval(5000)
		                .commandFrequency(commandFrequency)
		                .build())
		        .producerPolicy(ProducerPolicy.builder()
		                .priorityQueueEnabled(Boolean.TRUE)
		                .build())
		        .build();

		final Init init = Init.builder()
		        .delayAfterInit(0)
		        .protocol(Protocol.AUTO)
		        .sequence(DefaultCommandGroup.INIT)
		        .build();
		
		workflow.start(connection, query, init, optional);

		WorkflowFinalizer.finalizeAfter(workflow, 10000, () -> false);

		final PidDefinitionRegistry rpm = workflow.getPidRegistry();

		PidDefinition measuredPID = rpm.findBy(13l);
		double ratePerSec = workflow.getDiagnostics().rate().findBy(RateType.MEAN, measuredPID).get().getValue();

		log.info("Rate:{}  ->  {}", measuredPID, ratePerSec);

		Assertions.assertThat(ratePerSec).isGreaterThanOrEqualTo(commandFrequency);
	}
}
