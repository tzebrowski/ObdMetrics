package org.obd.metrics.batch;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.codec.batch.BatchCodec;
import org.obd.metrics.codec.batch.BatchMessagePatternEntry;
import org.obd.metrics.codec.batch.BatchMessage;
import org.obd.metrics.command.obd.BatchObdCommand;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.model.RawMessage;
import org.obd.metrics.pid.PidDefinitionRegistry;

public class Med17_5_BatchCodecTest {

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
			final String message = "00f0:41010007e1001:030000040005002:0680aaaaaaaaaa";
			BatchCodec codec = new BatchObdCommand(message, commands, 0);

			Map<ObdCommand, RawMessage> values = codec.decode(null,RawMessage.instance(message));
			
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("03")),BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("04")),BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("05")),BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("06")),BatchMessage.instance(message));

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
			final String message = "00c0:4105000bff0c1:00000f001100aa";
			final BatchCodec codec = new BatchObdCommand(message, commands, 0);

			Map<ObdCommand, RawMessage> values = codec.decode(null,RawMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("05")),BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0B")),BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")),BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0F")),BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("11")),BatchMessage.instance(message));
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
			final String message = "4105000c0000";
			final BatchCodec codec = new BatchObdCommand(message, commands, 0);

			Map<ObdCommand, RawMessage> values = codec.decode(null,RawMessage.instance(message));
			
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("05")),BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")),BatchMessage.instance(message));

			// 01 05 0C
			// 4105000c0000

			// 01 0B 0C 0D 0E 0F 11
			// 00e0:410bff0c00001:0d000e800f00112:00aaaaaaaaaaaa

			// 01 13 15 1C 1F
			// 00b0:411303155aff1:1c061f0000aaaa

			// 01 01 03 04 05 06 07
			// 0110:41010007e1001:030000040005002:0680078baaaaaa

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

			final String message = "0110:41010007e1001:030000040005002:0680078baaaaaa";
			final BatchCodec codec = new BatchObdCommand(message, commands, 0);

			Map<ObdCommand, RawMessage> values = codec.decode(null,RawMessage.instance(message));
			
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("01")), BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("03")), BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("04")), BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("05")), BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("06")), BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("07")), BatchMessage.instance(message));
			
			BatchMessage rawBatchMessage = (BatchMessage) values.get(new ObdCommand(registry.findBy("01")));
			BatchMessagePatternEntry pattern = rawBatchMessage.getPattern();

			Assertions.assertThat(pattern.getStart()).isEqualTo(7);
			Assertions.assertThat(pattern.getEnd()).isEqualTo(15);
		}
	}
}
