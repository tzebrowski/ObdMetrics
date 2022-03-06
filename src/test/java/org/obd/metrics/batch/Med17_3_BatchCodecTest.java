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
			//00A0:410BFF0C00001:11000D00AAAAAA

			final byte[] message = "00B0:410C000010001:000B660D000000".getBytes();
			final BatchCodec codec = BatchCodec.instance(null, commands);
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

			final byte[] message = "00F0:410C000010001:000B660D0005222:0F370000000000".getBytes();
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

			final byte[] message = "410C0000100000".getBytes();
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

			final byte[] message = "0090:410C000010001:000B6600000000".getBytes();
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

			final byte[] message = "00D0:410C000010001:000B660D000522".getBytes();
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

			final byte[] message = "00F0:410C000010001:000B660D0005222:11260000000000".getBytes();
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

			final byte[] message = "00F0:410B650C00001:0D000E800F2F102:00000000000000".getBytes();
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

			final byte[] message = "0110:4101000771611:0300000400051c2:06800781000000".getBytes();
			final BatchCodec codec = BatchCodec.instance(null, commands);
			final Map<ObdCommand, RawMessage> values = codec.decode(null,RawMessage.instance(message));
	
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("05")), BatchMessage.instance(message));
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("04")), BatchMessage.instance(message));
		}
	}
}
