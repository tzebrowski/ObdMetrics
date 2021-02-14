package org.obd.metrics.workflow;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;
import org.obd.metrics.command.group.AlfaMed17CommandGroup;
import org.obd.metrics.connection.MockedConnection;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidRegistry;
import org.obd.metrics.statistics.Statistics;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GenericWorkflowTest {
	
	@Test
	public void nonBatchTest() throws IOException, InterruptedException  {
		
		final Map<String, String> reqResp = new HashMap<String, String>();
		reqResp.put("221003", "62100340");
		reqResp.put("221000", "6210000BEA");
		reqResp.put("221935", "62193540");
		reqResp.put("22194f", "62194f2d85");
		reqResp.put("221812", "");
		
		final DataCollector collector = new DataCollector();

		final Workflow workflow = Workflow.generic()
				.equationEngine("JavaScript")
				.ecuSpecific(EcuSpecific
					.builder()
					.initSequence(AlfaMed17CommandGroup.CAN_INIT)
					.pidFile("alfa.json").build())
				.observer(collector). build();
		
		final Set<Long> ids = new HashSet<>();
		ids.add(8l); // Coolant
		ids.add(4l); // RPM
		ids.add(7l); // Intake temp
		ids.add(15l);// Oil temp
		ids.add(3l); // Spark Advance
		
		workflow.connection(new MockedConnection(reqResp)).filter(ids).batch(false).start();
		final Callable<String> end = () -> {
			Thread.sleep(1 * 10000);
			log.info("Ending the process of collecting the data");
			workflow.stop();
			return "end";
		};

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(1);
		newFixedThreadPool.invokeAll(Arrays.asList(end));
		
		final PidRegistry pids = workflow.getPids();

		PidDefinition pid8l = pids.findBy(8l);
		Statistics stat8l = workflow.getStatistics().findBy(pid8l);
		Assertions.assertThat(stat8l).isNotNull();
		
		PidDefinition pid4l = pids.findBy(4l);
		Statistics stat4L = workflow.getStatistics().findBy(pid4l);
		Assertions.assertThat(stat4L).isNotNull();

		Assertions.assertThat(stat4L.getMax()).isEqualTo(762);
		Assertions.assertThat(stat4L.getMin()).isEqualTo(762);
		Assertions.assertThat(stat4L.getMedian()).isEqualTo(762);
		
		Assertions.assertThat(stat8l.getMax()).isEqualTo(-1);
		Assertions.assertThat(stat8l.getMin()).isEqualTo(-1);
		Assertions.assertThat(stat8l.getMedian()).isEqualTo(-1);
		
		final double ratePerSec1003 = workflow.getStatistics().getRatePerSec(pid8l);
		final double ratePerSec1000 = workflow.getStatistics().getRatePerSec(pid4l);

		log.info("Rate: 1003: {}/sec", ratePerSec1003);
		log.info("Rate: 1000: {}/sec", ratePerSec1000);

		Assertions.assertThat(ratePerSec1003).isGreaterThan(10d);
		Assertions.assertThat(ratePerSec1000).isGreaterThan(10d);
		
		newFixedThreadPool.shutdown();
	}
}
