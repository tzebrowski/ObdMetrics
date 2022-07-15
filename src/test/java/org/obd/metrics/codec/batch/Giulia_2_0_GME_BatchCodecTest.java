package org.obd.metrics.codec.batch;

import static org.obd.metrics.codec.batch.BatchMessageBuilder.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.PidRegistryCache;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.raw.RawMessage;

public class Giulia_2_0_GME_BatchCodecTest {

	@Test
	public void case_01() {
		final PidDefinitionRegistry registry = PidRegistryCache.get("giulia_2.0_gme.json");
		List<ObdCommand> commands = new ArrayList<>();
		commands.add(new ObdCommand(registry.findBy(7001l)));
		commands.add(new ObdCommand(registry.findBy(7002l)));
		commands.add(new ObdCommand(registry.findBy(7003l)));
		final byte[] message = "00C0:62195A03EC191:355E13020060".getBytes();
		final BatchCodec codec = BatchCodec.instance(null, commands);

		Map<ObdCommand, RawMessage> values = codec.decode(null, RawMessage.wrap(message));
	
		final BatchMessage batchMessage = instance(message);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy(7001l)), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy(7002l)), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy(7003l)), batchMessage);
	}
}
