package org.obd.metrics.codec.batch;

import static org.obd.metrics.codec.batch.mapper.BatchMessageBuilder.instance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.PIDsRegistry;
import org.obd.metrics.PIDsRegistryFactory;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

public class Giulia_2_0_GME_BatchCodecTest {

	@Test
	public void case_01() {
		final PidDefinitionRegistry registry = PIDsRegistryFactory.get("giulia_2.0_gme.json");
		List<ObdCommand> commands = new ArrayList<>();
		commands.add(new ObdCommand(registry.findBy(7001l)));
		commands.add(new ObdCommand(registry.findBy(7002l)));
		commands.add(new ObdCommand(registry.findBy(7003l)));
		final byte[] message = "00C0:62195A03EC191:355E13020060".getBytes();
		final BatchCodec codec = BatchCodec.builder().commands(commands).build();
		final Map<ObdCommand, ConnectorResponse> values = codec.decode(ConnectorResponseFactory.wrap(message));

	
		final ConnectorResponse batchMessage = instance(message);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy(7001l)), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy(7002l)), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy(7003l)), batchMessage);
	}
	
	@Test
	public void case_02() {
		
		final PIDsRegistry registry = PIDsRegistryFactory.get("giulia_2.0_gme.json");

		final List<ObdCommand> commands = Arrays.asList("181F 1937 130A 1924 1935 1302 3A58 18BA 1004"
				.split(" "))
				.stream()
				.map(pid-> new ObdCommand(registry.findBy(pid)))
				.collect(Collectors.toList());
		
		
		//STPX H:18DA10F1, D:22 181F 1937 130A 1924 1935 1302 3A58 18BA 1004, R:5
		final byte[] message = "0200:62181F03E4191:3703D9130A19192:240019353913023:00123A583818BA4:681004007A".getBytes();
		final BatchCodec codec = BatchCodec.builder().commands(commands).build();
		final Map<ObdCommand, ConnectorResponse> values = codec.decode(ConnectorResponseFactory.wrap(message));

		final ConnectorResponse batchMessage = instance(message);

		for (final ObdCommand cmd : commands) {
			Assertions.assertThat(values).containsEntry(cmd, batchMessage);	
		}
	
	}
	
	@Test
	public void case_04() {
		final PIDsRegistry registry = PIDsRegistryFactory.get("giulia_2.0_gme.json");

		final List<ObdCommand> commands = Arrays.asList("181F 1937 130A 1924"
				.split(" "))
				.stream()
				.map(pid-> new ObdCommand(registry.findBy(pid)))
				.collect(Collectors.toList());
		
		
		final byte[] message = "00F0:62181F03DE191:3703D9130A19192:2400".getBytes();
		final BatchCodec codec = BatchCodec.builder().commands(commands).build();
		final Map<ObdCommand, ConnectorResponse> values = codec.decode(ConnectorResponseFactory.wrap(message));

		final ConnectorResponse batchMessage = instance(message);

		for (final ObdCommand cmd : commands) {
			Assertions.assertThat(values).containsEntry(cmd, batchMessage);	
		}
	}
	
	
	@Test
	public void case_05() {
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

		final ConnectorResponse batchMessage = instance(message);

		for (final ObdCommand cmd : commands) {
			Assertions.assertThat(values).containsEntry(cmd, batchMessage);	
		}
		
	}
	
	
	@Test
	public void case_06() {
		final PIDsRegistry registry = PIDsRegistryFactory.get("giulia_2.0_gme.json");

		final List<ObdCommand> commands = Arrays.asList("181F 1937 130A 1924 1956 1935 1302 1837 3A58 18BA"
				.split(" "))
				.stream()
				.map(pid-> new ObdCommand(registry.findBy(pid)))
				.collect(Collectors.toList());
		
		
		//STPX H:18DA10F1, D:22 181F 1937 130A 1924 1956 1935 1302 1837 3A58 18BA, R:5
		final byte[] message = "0230:62181F03F4191:3703E6130A19192:2400195603F7193:353E13020014184:370E3A583F18BA".getBytes();
		final BatchCodec codec = BatchCodec.builder().commands(commands).build();
		final Map<ObdCommand, ConnectorResponse> values = codec.decode(ConnectorResponseFactory.wrap(message));

		final ConnectorResponse batchMessage = instance(message);

		for (final ObdCommand cmd : commands) {
			Assertions.assertThat(values).containsEntry(cmd, batchMessage);	
		}
	}
}
