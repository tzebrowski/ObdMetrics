package org.obd.metrics.codec.batch;

import static org.obd.metrics.codec.batch.mapper.BatchMessageBuilder.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.PidRegistryCache;
import org.obd.metrics.codec.batch.mapper.BatchMessage;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

public class Giulia_2_0_GME_BatchCodecTest {

	@Test
	public void case_01() {
		final PidDefinitionRegistry registry = PidRegistryCache.get("giulia_2.0_gme.json");
		List<ObdCommand> commands = new ArrayList<>();
		commands.add(new ObdCommand(registry.findBy(7001l)));
		commands.add(new ObdCommand(registry.findBy(7002l)));
		commands.add(new ObdCommand(registry.findBy(7003l)));
		final byte[] message = "00C0:62195A03EC191:355E13020060".getBytes();
		final BatchCodec codec = BatchCodec.builder().commands(commands).build();
		final Map<ObdCommand, ConnectorResponse> values = codec.decode(null, ConnectorResponseFactory.wrap(message));

	
		final BatchMessage batchMessage = instance(message);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy(7001l)), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy(7002l)), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy(7003l)), batchMessage);
	}
	
	@Test
	public void case_02() {
		final PidDefinitionRegistry registry = PidRegistryCache.get("giulia_2.0_gme.json");
		List<ObdCommand> commands = new ArrayList<>();
		commands.add(new ObdCommand(registry.findBy("181F")));
		commands.add(new ObdCommand(registry.findBy("1937")));
		commands.add(new ObdCommand(registry.findBy("130A")));
		commands.add(new ObdCommand(registry.findBy("1924")));
		commands.add(new ObdCommand(registry.findBy("1935")));
		commands.add(new ObdCommand(registry.findBy("1302")));
		commands.add(new ObdCommand(registry.findBy("3A58")));
		commands.add(new ObdCommand(registry.findBy("18BA")));
		commands.add(new ObdCommand(registry.findBy("1004")));
		
		//STPX H:18DA10F1, D:22 181F 1937 130A 1924 1935 1302 3A58 18BA 1004, R:5
		final byte[] message = "0200:62181F03E4191:3703D9130A19192:240019353913023:00123A583818BA4:681004007A".getBytes();
		final BatchCodec codec = BatchCodec.builder().commands(commands).build();
		final Map<ObdCommand, ConnectorResponse> values = codec.decode(null, ConnectorResponseFactory.wrap(message));

	
		final BatchMessage batchMessage = instance(message);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("181F")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("1937")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("130A")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("1924")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("1935")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("1302")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("3A58")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("18BA")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("1004")), batchMessage);
	
	}
	
	@Test
	public void case_04() {
		final PidDefinitionRegistry registry = PidRegistryCache.get("giulia_2.0_gme.json");
		List<ObdCommand> commands = new ArrayList<>();
		commands.add(new ObdCommand(registry.findBy("181F")));
		commands.add(new ObdCommand(registry.findBy("1937")));
		commands.add(new ObdCommand(registry.findBy("130A")));
		commands.add(new ObdCommand(registry.findBy("1924")));
		
		// STPX H:18DA10F1, D:22 181F 1937 130A 1924, R:3
		final byte[] message = "00F0:62181F03DE191:3703D9130A19192:2400".getBytes();
		final BatchCodec codec = BatchCodec.builder().commands(commands).build();
		final Map<ObdCommand, ConnectorResponse> values = codec.decode(null, ConnectorResponseFactory.wrap(message));

		final BatchMessage batchMessage = instance(message);
		
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("181F")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("1937")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("130A")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("1924")), batchMessage);
	}
}
