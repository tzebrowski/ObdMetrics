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
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

public class Giulia_2_0_GME_CodecTest {
	
	@Test
	public void messageDecodingTest() {
		final PIDsRegistry registry = PIDsRegistryFactory.get("giulia_2.0_gme.json");

		final List<ObdCommand> commands = Arrays.asList("181F 1937 130A 1924 1956 1935 1302 1837 3A58 18BA 1004"
				.split(" "))
				.stream()
				.map(pid-> new ObdCommand(registry.findBy(pid)))
				.collect(Collectors.toList());
		
		
		// STPX H:18DA10F1, D:22 181F 1937 130A 1924 1956 1935 1302 1837 3A58 18BA 1004, R:6
		final byte[] message = "0270:62181F03F5191:3703E9130A19192:2400195603F7193:353D13020014184:370E3A583F18BA5:7510040079".getBytes();
		final BatchCodec codec = BatchCodec.builder().commands(commands).build();
		final Map<ObdCommand, ConnectorResponse> values = codec.decode(ConnectorResponseFactory.wrap(message));

		final ConnectorResponse connectorResponse = instance(message);

		for (final ObdCommand cmd : commands) {
			Assertions.assertThat(values).containsEntry(cmd, connectorResponse);
		}
		
		final CodecRegistry codecRegistry = CodecRegistry.builder().adjustments(Adjustments.DEFAULT).build();
		final Map<String, Object> expectedValues = new HashMap<>();
		expectedValues.put("1004", 12.1);
		expectedValues.put("181F", 1013.0);
		expectedValues.put("1937", 1001.0);
		expectedValues.put("130A", -0.0);
		expectedValues.put("1924", 0.0);
		expectedValues.put("1956", 1015.0);
		expectedValues.put("1935", 21.0);
		expectedValues.put("1302", 20.0);
		expectedValues.put("1837", 62.22);
		expectedValues.put("3A58", 23.0);
		expectedValues.put("18BA", 530.12);
				
		
		commands.forEach(c -> {
			final ConnectorResponse cr = values.get(c);
			final Object value = codecRegistry.findCodec(c.getPid()).decode(c.getPid(), cr);
			final Object expected = expectedValues.get(c.getPid().getPid());
			
			Assertions
			.assertThat(value)
			.overridingErrorMessage("PID: %s, expected: %s",value.toString(),c.getPid().getPid())
			.isEqualTo(expected);
		});
		
	}
}
