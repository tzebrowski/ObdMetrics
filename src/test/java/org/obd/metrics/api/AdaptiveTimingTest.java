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
import org.obd.metrics.Reply;
import org.obd.metrics.command.at.CustomATCommand;
import org.obd.metrics.command.group.AlfaMed17CommandGroup;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.Urls;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AdaptiveTimingTest {

	@Test
	public void recieveReplyTest() throws IOException, InterruptedException {

		final DataCollector collector = new DataCollector();

		final int commandFrequency = 5;

		final Workflow workflow = WorkflowFactory.generic()
		        .pidSpec(PidSpec
		                .builder()
		                .initSequence(AlfaMed17CommandGroup.CAN_INIT_NO_DELAY)
		                .pidFile(Urls.resourceToUrl("alfa.json")).build())
		        .observer(collector)
		        .initialize();

		final Set<Long> ids = new HashSet<>();
		ids.add(8l); // Coolant
		ids.add(4l); // RPM
		ids.add(7l); // Intake temp
		ids.add(15l);// Oil temp
		ids.add(3l); // Spark Advance

		final MockConnection connection = MockConnection.builder()
		        .commandReply("221003", "62100340")
		        .commandReply("221000", "6210000BEA")
		        .commandReply("221935", "62193540")
		        .commandReply("22194f", "62194f2d85")
		        .readTimeout(4)
		        .build();

		workflow.start(WorkflowContext
		        .builder()
		        .adaptiveTiming(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.TRUE)
		                .checkInterval(20)// 20ms
		                .commandFrequency(commandFrequency)
		                .build())
		        .connection(connection)
		        .filter(ids).build());

		final Callable<String> end = () -> {
			Thread.sleep(1 * 1500);
			log.info("Ending the process of collecting the data");
			workflow.stop();
			return "end";
		};

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(1);
		newFixedThreadPool.invokeAll(Arrays.asList(end));
		newFixedThreadPool.shutdown();

		// Ensure we receive AT command as well
		Reply<?> at = collector.getData().get(new CustomATCommand("Z")).iterator().next();
		Assertions.assertThat(at).isNotNull();

		final PidDefinition pid = workflow.getPidRegistry().findBy(4l);
		final double ratePerSec = workflow.getStatisticsRegistry().getRatePerSec(pid);
		Assertions.assertThat(ratePerSec)
		        .isGreaterThanOrEqualTo(commandFrequency);
	}

}
