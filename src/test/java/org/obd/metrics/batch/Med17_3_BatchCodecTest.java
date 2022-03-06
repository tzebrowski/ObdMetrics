package org.obd.metrics.batch;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.codec.batch.BatchCodec;
import org.obd.metrics.codec.batch.BatchMessage;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.model.RawMessage;
import org.obd.metrics.pid.PidDefinitionRegistry;

public class Med17_3_BatchCodecTest {

	@Test
	public void case_01() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
		        .getResourceAsStream("mode01.json")) {

			PidDefinitionRegistry registry = PidDefinitionRegistry.builder().source(source).build();
			List<ObdCommand> commands = new ArrayList<>();
			commands.add(new ObdCommand(registry.findBy("0C")));
			commands.add(new ObdCommand(registry.findBy("10")));
			commands.add(new ObdCommand(registry.findBy("0B")));
			commands.add(new ObdCommand(registry.findBy("0D")));
			commands.add(new ObdCommand(registry.findBy("05")));
			//00A0:410BFF0C00001:11000D00AAAAAA

			final String message = "00b0:410c000010001:000b660d000000";
			final BatchCodec codec = BatchCodec.instance(message, commands);
			final Map<ObdCommand, RawMessage> values = codec.decode(null,RawMessage.instance(message));
			
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0B")), BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")), BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0D")), BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("10")), BatchMessage.instance(message));
		}
	}

	@Test
	public void case_02() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
		        .getResourceAsStream("mode01.json")) {
			
			PidDefinitionRegistry registry = PidDefinitionRegistry.builder().source(source).build();
			List<ObdCommand> commands = new ArrayList<>();
			commands.add(new ObdCommand(registry.findBy("0C")));
			commands.add(new ObdCommand(registry.findBy("10")));
			commands.add(new ObdCommand(registry.findBy("0B")));
			commands.add(new ObdCommand(registry.findBy("0D")));
			commands.add(new ObdCommand(registry.findBy("05")));
			commands.add(new ObdCommand(registry.findBy("0F")));

			final String message = "00f0:410c000010001:000b660d0005222:0f370000000000";
			final BatchCodec codec = BatchCodec.instance(null, commands);
			final Map<ObdCommand, RawMessage> values = codec.decode(null,RawMessage.instance(message));
			
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0B")), BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")), BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0F")), BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("05")), BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0D")), BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("10")), BatchMessage.instance(message));
		}
	}

	@Test
	public void case_03() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
		        .getResourceAsStream("mode01.json")) {
			
			PidDefinitionRegistry registry = PidDefinitionRegistry.builder().source(source).build();
			List<ObdCommand> commands = new ArrayList<>();
			commands.add(new ObdCommand(registry.findBy("0C")));
			commands.add(new ObdCommand(registry.findBy("10")));
			commands.add(new ObdCommand(registry.findBy("0B")));
			commands.add(new ObdCommand(registry.findBy("0D")));
			commands.add(new ObdCommand(registry.findBy("05")));
			commands.add(new ObdCommand(registry.findBy("0F")));

			final String message = "410c0000100000";
			final BatchCodec codec = BatchCodec.instance(null, commands);
			final Map<ObdCommand, RawMessage> values = codec.decode(null,RawMessage.instance(message));
	
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")), BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("10")), BatchMessage.instance(message));
		}
	}

	@Test
	public void case_04() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
		        .getResourceAsStream("mode01.json")) {
			
			PidDefinitionRegistry registry = PidDefinitionRegistry.builder().source(source).build();
			List<ObdCommand> commands = new ArrayList<>();
			commands.add(new ObdCommand(registry.findBy("0C")));
			commands.add(new ObdCommand(registry.findBy("10")));
			commands.add(new ObdCommand(registry.findBy("0B")));
			commands.add(new ObdCommand(registry.findBy("0D")));
			commands.add(new ObdCommand(registry.findBy("05")));
			commands.add(new ObdCommand(registry.findBy("0F")));

			final String message = "0090:410c000010001:000b6600000000";
			final BatchCodec codec = BatchCodec.instance(null, commands);
			final Map<ObdCommand, RawMessage> values = codec.decode(null,RawMessage.instance(message));
		
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")), BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("10")), BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0B")), BatchMessage.instance(message));
		}
	}

	@Test
	public void case_05() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
		        .getResourceAsStream("mode01.json")) {
			
			PidDefinitionRegistry registry = PidDefinitionRegistry.builder().source(source).build();
			List<ObdCommand> commands = new ArrayList<>();
			commands.add(new ObdCommand(registry.findBy("0C")));
			commands.add(new ObdCommand(registry.findBy("10")));
			commands.add(new ObdCommand(registry.findBy("0B")));
			commands.add(new ObdCommand(registry.findBy("0D")));
			commands.add(new ObdCommand(registry.findBy("05")));

			final String message = "00d0:410c000010001:000b660d000522";
			final BatchCodec codec = BatchCodec.instance(null, commands);
			final Map<ObdCommand, RawMessage> values = codec.decode(null,RawMessage.instance(message));
			
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0B")), BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")), BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("05")), BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0D")), BatchMessage.instance(message));
		}
	}

	@Test
	public void case_06() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
		        .getResourceAsStream("mode01.json")) {
			
			PidDefinitionRegistry registry = PidDefinitionRegistry.builder().source(source).build();
			List<ObdCommand> commands = new ArrayList<>();
			commands.add(new ObdCommand(registry.findBy("0C")));
			commands.add(new ObdCommand(registry.findBy("10")));
			commands.add(new ObdCommand(registry.findBy("0B")));
			commands.add(new ObdCommand(registry.findBy("0D")));
			commands.add(new ObdCommand(registry.findBy("05")));
			commands.add(new ObdCommand(registry.findBy("11")));

			final String message = "00f0:410c000010001:000b660d0005222:11260000000000";
			final BatchCodec decoder = BatchCodec.instance(null, commands);
			final Map<ObdCommand, RawMessage> values = decoder.decode(null,RawMessage.instance(message));
			
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0B")), BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")), BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("11")), BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("05")), BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0D")), BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("10")), BatchMessage.instance(message));
		}
	}

	@Test
	public void case_07() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
		        .getResourceAsStream("mode01.json")) {
			
			PidDefinitionRegistry registry = PidDefinitionRegistry.builder().source(source).build();
			List<ObdCommand> commands = new ArrayList<>();
			commands.add(new ObdCommand(registry.findBy("0B")));
			commands.add(new ObdCommand(registry.findBy("0C")));
			commands.add(new ObdCommand(registry.findBy("0D")));
			commands.add(new ObdCommand(registry.findBy("0E")));
			commands.add(new ObdCommand(registry.findBy("0F")));
			commands.add(new ObdCommand(registry.findBy("10")));

			final String message = "00f0:410b650c00001:0d000e800f2f102:00000000000000";
			final BatchCodec codec = BatchCodec.instance(null, commands);
			final Map<ObdCommand, RawMessage> values = codec.decode(null,RawMessage.instance(message));
			
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0E")), BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0D")), BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")), BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0B")), BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0D")), BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("10")), BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0F")), BatchMessage.instance(message));
		}
	}

	@Test
	public void case_08() throws IOException {
		// 01 03 04 05 06 07
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

			final String message = "0110:4101000771611:0300000400051c2:06800781000000";
			final BatchCodec codec = BatchCodec.instance(null, commands);
			final Map<ObdCommand, RawMessage> values = codec.decode(null,RawMessage.instance(message));
	
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("05")), BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("04")), BatchMessage.instance(message));
		}
	}
}
