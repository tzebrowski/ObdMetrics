package org.obd.metrics.codec.batch;

import static org.obd.metrics.codec.batch.mapper.BatchMessageBuilder.instance;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.PIDsRegistry;
import org.obd.metrics.PIDsRegistryFactory;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Giulia_2_0_GME_CodecTest {

	@Test
	public void case01() {

		final Map<String, Object> expectedValues = new HashMap<>();
		expectedValues.put("1004", 12.1);
		expectedValues.put("181F", 1013.0);
		expectedValues.put("1937", 1001.0);
		expectedValues.put("130A", 0.0);
		expectedValues.put("1924", 0.0);
		expectedValues.put("1956", 1015.0);
		expectedValues.put("1935", 21.0);
		expectedValues.put("1302", 20.0);
		expectedValues.put("1837", 62.22);
		expectedValues.put("3A58", 23.0);
		expectedValues.put("18BA", 530.12);

		final String query = "181F 1937 130A 1924 1956 1935 1302 1837 3A58 18BA 1004";
		final String ecuAnswer = "0270:62181F03F5191:3703E9130A19192:2400195603F7193:353D13020014184:370E3A583F18BA5:7510040079";

		performTest(query, ecuAnswer, expectedValues);
	}

	@Test
	public void case02() {

		final Map<String, Object> expectedValues = new HashMap<>();
		expectedValues.put("1004", 12.1);
		expectedValues.put("181F", 1006.0);
		expectedValues.put("1937", 1001.0);
		expectedValues.put("130A", 0.0);
		expectedValues.put("1924", 0.0);
		expectedValues.put("1956", 1015.0);
		expectedValues.put("1935", 21.0);
		expectedValues.put("1302", 20.0);
		expectedValues.put("1837", 62.22);
		expectedValues.put("3A58", 23.0);
		expectedValues.put("18BA", -77.03);
		expectedValues.put("1935", 22.0);

		final String query = "181F 1937 130A 1924 1956 1935 1302 1837 3A58 18BA";
		final String ecuAnswer = "0230:62181F03EE191:3703E9130A19192:2400195603F7193:353E13020014184:370E3A583F18BA";

		performTest(query, ecuAnswer, expectedValues);
	}

	@Test
	public void case03() {

		final Map<String, Object> expectedValues = new HashMap<>();
		expectedValues.put("1004", 12.1);
		expectedValues.put("181F", 1008.0);
		expectedValues.put("1937", 1001.0);
		expectedValues.put("130A", 0.04);
		expectedValues.put("1924", 0.0);
		expectedValues.put("1956", 1016.0);
		expectedValues.put("1935", 15.0);
		expectedValues.put("1302", 15.0);
		expectedValues.put("1837", 57.78);
		expectedValues.put("3A58", 15.0);
		expectedValues.put("18BA", 525.59);

		final String query = "181F 1937 130A 1924 1956 1935 1302 1837 3A58 18BA 1004";
		final String ecuAnswer = "0270:62181F03F0191:3703E9130A1A192:2400195603F8193:35371302000F184:370D3A583718BA5:7410040079";
		
		performTest(query, ecuAnswer, expectedValues);
	}

	@Test
	public void case04() {

		final Map<String, Object> expectedValues = new HashMap<>();
		expectedValues.put("181F", 1008.0);
		expectedValues.put("1937", 1001.0);
		expectedValues.put("130A", 0.04);
		expectedValues.put("1924", 0.0);
		expectedValues.put("1956", 1016.0);
		expectedValues.put("1935", 15.0);
		expectedValues.put("1302", 15.0);
		expectedValues.put("1837", 57.78);
		expectedValues.put("3A58", 15.0);
		expectedValues.put("18BA", 525.59);

		final String query = "181F 1937 130A 1924 1956 1935 1302 1837 3A58 18BA";
		final String ecuAnswer = "0230:62181F03F0191:3703E9130A1A192:2400195603F8193:35371302000F184:370D3A583718BA5:74";

		performTest(query, ecuAnswer, expectedValues);
	}
	
	@Test
	public void case05() {

		final Map<String, Object> expectedValues = new HashMap<>();
		expectedValues.put("181F", 1007.0);
		expectedValues.put("1937", 1001.0);
		expectedValues.put("130A", 0.00);
		expectedValues.put("1924", 0.0);
		expectedValues.put("1956", 1016.0);
		expectedValues.put("1935", 70.0);
		expectedValues.put("1302", 99.0);
		expectedValues.put("1837", 311.11);
		expectedValues.put("3A58", 71.0);
		expectedValues.put("18BA", 484.81);
		expectedValues.put("1004", 12.5);

		final String query = "181F 1937 130A 1924 1935 1302 1837 3A58 18BA 1004";
		final String ecuAnswer = "0230:62181F03EF191:3703E9130A19192:240019356E13023:00631837463A584:6F18BA6B1004005:7D";

		performTest(query, ecuAnswer, expectedValues);
	}
	
	@Test
	public void case06() {

		final Map<String, Object> expectedValues = new HashMap<>();
		expectedValues.put("181F", 1007.0);
		expectedValues.put("1937", 1001.0);
		expectedValues.put("130A", 0.00);
		expectedValues.put("1924", 0.0);
		expectedValues.put("1956", 1016.0);
		expectedValues.put("1935", 70.0);
		expectedValues.put("1302", 96.0);
		expectedValues.put("1837", 275.56);
		expectedValues.put("3A58", 69.0);
		expectedValues.put("18BA", 484.81);
		expectedValues.put("1004", 12.4);
		expectedValues.put("1000", 58.25);

		final String query = "181F 1937 1000 130A 1924 1935 1302 1837 3A58 18BA 1004";
		final String ecuAnswer = "0270:62181F03EF191:3703E9100000002:130A19192400193:356E13020060184:373E3A586D18BA5:6B1004007C";

		performTest(query, ecuAnswer, expectedValues);
	}

	void performTest(String query, String ecuAnswer, Map<String, Object> expectedValues) {
		final PIDsRegistry registry = PIDsRegistryFactory.get("giulia_2.0_gme.json");

		final List<ObdCommand> commands = Arrays.asList(query.split(" ")).stream()
				.map(pid -> new ObdCommand(registry.findBy(pid))).collect(Collectors.toList());

		final byte[] message = ecuAnswer.getBytes();
		final BatchCodec codec = BatchCodec.builder().commands(commands).build();
		final Map<ObdCommand, ConnectorResponse> values = codec.decode(ConnectorResponseFactory.wrap(message));

		final ConnectorResponse connectorResponse = instance(message);

		Assertions.assertThat(values).isNotEmpty();
		Assertions.assertThat(values).hasSize(commands.size());
		
		for (final ObdCommand cmd : commands) {
			Assertions.assertThat(values).containsEntry(cmd, connectorResponse);
		}

		final CodecRegistry codecRegistry = CodecRegistry.builder()
				.adjustments(Adjustments.DEFAULT)
				.formulaEvaluatorConfig(FormulaEvaluatorConfig.builder().debug(true).build()).build();

		commands.forEach(c -> {
			final ConnectorResponse cr = values.get(c);
			final Object value = codecRegistry.findCodec(c.getPid()).decode(c.getPid(), cr);
			final String pid = c.getPid().getPid();
			final Object expected = expectedValues.get(pid);

			log.info("PID={}, expected={}, evaluated={},mapping={}", pid, expected, value, cr);

			Assertions.assertThat(value).overridingErrorMessage("PID: %s, expected: %s", pid, value)
					.isEqualTo(expected);
		});
	}
}
