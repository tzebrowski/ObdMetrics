package org.obd.metrics.codec.batch;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.raw.RawMessage;

public class Med17_5_BatchCodecTest {
	
	static BatchMessage instance(byte[] message) {
		return new BatchMessage(null, message);
	}
	
	@Test
	public void case_01() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
		        .getResourceAsStream("mode01.json")) {

			PidDefinitionRegistry registry = PidDefinitionRegistry.builder().source(source).build();
			List<ObdCommand> commands = new ArrayList<>();
			commands.add(new ObdCommand(registry.findBy("01")));
			commands.add(new ObdCommand(registry.findBy("03")));
			commands.add(new ObdCommand(registry.findBy("04")));
			commands.add(new ObdCommand(registry.findBy("05")));
			commands.add(new ObdCommand(registry.findBy("06")));
			final byte[] message = "00F0:41010007E1001:030000040005002:0680AAAAAAAAAA".getBytes();
			final BatchCodec codec = BatchCodec.instance(null, commands);

			Map<ObdCommand, RawMessage> values = codec.decode(null,RawMessage.wrap(message));
			
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("03")),instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("04")),instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("05")),instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("06")),instance(message));
		}
	}

	@Test
	public void case_02() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
		        .getResourceAsStream("mode01.json")) {

			PidDefinitionRegistry registry = PidDefinitionRegistry.builder().source(source).build();
			List<ObdCommand> commands = new ArrayList<>();
			commands.add(new ObdCommand(registry.findBy("05")));
			commands.add(new ObdCommand(registry.findBy("0B")));
			commands.add(new ObdCommand(registry.findBy("0C")));
			commands.add(new ObdCommand(registry.findBy("0F")));
			commands.add(new ObdCommand(registry.findBy("11")));
			final byte[] message = "00C0:4105000BFF0C1:00000F001100AA".getBytes();
			final BatchCodec codec = BatchCodec.instance(null, commands);

			Map<ObdCommand, RawMessage> values = codec.decode(null,RawMessage.wrap(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("05")),instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0B")),instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")),instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0F")),instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("11")),instance(message));
		}
	}

	@Test
	public void case_03() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
		        .getResourceAsStream("mode01.json")) {

			PidDefinitionRegistry registry = PidDefinitionRegistry.builder().source(source).build();
			List<ObdCommand> commands = new ArrayList<>();
			commands.add(new ObdCommand(registry.findBy("05")));
			commands.add(new ObdCommand(registry.findBy("0C")));
			final byte[] message = "4105000C0000".getBytes();
			final BatchCodec codec = BatchCodec.instance(null, commands);

			Map<ObdCommand, RawMessage> values = codec.decode(null,RawMessage.wrap(message));
			
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("05")),instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")),instance(message));
		}
	}

	@Test
	public void case_04() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
		        .getResourceAsStream("mode01.json")) {

			PidDefinitionRegistry registry = PidDefinitionRegistry.builder().source(source).build();
			List<ObdCommand> commands = new ArrayList<>();
			commands.add(new ObdCommand(registry.findBy("01")));
			commands.add(new ObdCommand(registry.findBy("03")));
			commands.add(new ObdCommand(registry.findBy("04")));
			commands.add(new ObdCommand(registry.findBy("05")));
			commands.add(new ObdCommand(registry.findBy("06")));
			commands.add(new ObdCommand(registry.findBy("07")));

			final byte[] message = "0110:41010007E1001:030000040005002:0680078BAAAAAA".getBytes();
			final BatchCodec codec = BatchCodec.instance(null, commands);
			final Map<ObdCommand, RawMessage> values = codec.decode(null,RawMessage.wrap(message));
			
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("01")), instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("03")), instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("04")), instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("05")), instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("06")), instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("07")), instance(message));
		}
	}
}
