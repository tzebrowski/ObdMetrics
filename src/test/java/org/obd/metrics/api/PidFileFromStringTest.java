package org.obd.metrics.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;
import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.api.model.Pids;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.connection.SimpleMockConnection;
import org.obd.metrics.pid.Urls;

public class PidFileFromStringTest {

	@Test
	public void test() throws IOException, InterruptedException {

		String mode01 = getFileString();
		DataCollector collector = new DataCollector();
		Workflow workflow = Workflow
		        .instance()
		        .formulaEvaluatorConfig(FormulaEvaluatorConfig.builder().build())
		        .pids(Pids
		                .builder()
		                .resource(Urls.stringToUrl("mode01", mode01)).build())
		        .observer(collector)
		        .initialize();

		Query query = Query.builder()
		        .pid(6l) // Engine coolant temperature
		        .pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        .pid(16l) // Intake air temperature
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .build();

		final SimpleMockConnection connection = SimpleMockConnection.builder()
		        .commandReply("0100", "4100be3ea813")
		        .commandReply("0200", "4140fed00400")
		        .commandReply("0105", "410522")
		        .commandReply("010C", "410c541B")
		        .commandReply("010D", "")
		        .commandReply("0111", "no data")
		        .commandReply("010B", "410b35")
		        .readTimeout(0)
		        .build();

		workflow.start(connection, query);

		WorkflowFinalizer.finalizeAfter500ms(workflow);

		// Ensure we receive AT command as well
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();

		ObdMetric metric = collector.findSingleMetricBy(workflow.getPidRegistry().findBy(6l));

		Assertions.assertThat(metric.getValue()).isInstanceOf(Integer.class);
		Assertions.assertThat(metric.getValue()).isEqualTo(-6);
		Assertions.assertThat(metric.valueToDouble()).isEqualTo(-6.0);
		Assertions.assertThat(metric.valueToString()).isEqualTo("-6");

	}

	String getFileString() {
		String mode01 = new BufferedReader(
		        new InputStreamReader(Thread
		                .currentThread()
		                .getContextClassLoader()
		                .getResourceAsStream("mode01.json"), StandardCharsets.UTF_8))
		                        .lines()
		                        .collect(Collectors.joining("\n"));
		return mode01;
	}
}
