package org.obd.metrics.codec.batch;

import static org.obd.metrics.codec.batch.BatchMessageBuilder.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.PidRegistryCache;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.raw.RawMessage;

public class Med_17_3_Mode22_BatchCodedTest {
	
	@Test
	public void case_01(){
		final PidDefinitionRegistry registry = PidRegistryCache.get("alfa.json");
		List<ObdCommand> commands = new ArrayList<>();
		commands.add(new ObdCommand(registry.findBy("1867")));
		commands.add(new ObdCommand(registry.findBy("180E")));

		final byte[] message = "0090:6218670000181:0E0000".getBytes();
		final BatchCodec codec = BatchCodec.instance(Adjustments.DEFAULT, null, commands);
		final Map<ObdCommand, RawMessage> values = codec.decode(null, RawMessage.wrap(message));


		final BatchMessage batchMessage = instance(message);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("1867")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("180E")), batchMessage);
	}

	@Test
	public void case_02() {
		final PidDefinitionRegistry registry = PidRegistryCache.get("alfa.json");
		List<ObdCommand> commands = new ArrayList<>();
		commands.add(new ObdCommand(registry.findBy("194F")));
		commands.add(new ObdCommand(registry.findBy("1003")));
		commands.add(new ObdCommand(registry.findBy("1935")));

		final byte[] message = "00B0:62194F2E65101:0348193548".getBytes();
		final BatchCodec codec = BatchCodec.instance(Adjustments.DEFAULT, null, commands);
		final Map<ObdCommand, RawMessage> values = codec.decode(null, RawMessage.wrap(message));


		final BatchMessage batchMessage = instance(message);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("194F")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("1003")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("1935")), batchMessage);
	}
}
