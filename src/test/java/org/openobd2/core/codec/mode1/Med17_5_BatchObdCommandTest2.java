package org.openobd2.core.codec.mode1;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.batch.Batchable;
import org.openobd2.core.command.obd.BatchObdCommand;
import org.openobd2.core.command.obd.ObdCommand;
import org.openobd2.core.pid.PidRegistry;

public class Med17_5_BatchObdCommandTest2 {

	@Test
	public void t0() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("mode01.json")) {

			PidRegistry registry = PidRegistry.builder().source(source).build();
			List<ObdCommand> commands = new ArrayList<>();
			commands.add(new ObdCommand(registry.findBy("01")));
			commands.add(new ObdCommand(registry.findBy("02")));
			commands.add(new ObdCommand(registry.findBy("03")));
			commands.add(new ObdCommand(registry.findBy("04")));
			commands.add(new ObdCommand(registry.findBy("05")));
			commands.add(new ObdCommand(registry.findBy("06")));
			String message ="00f0:41010007e1001:030000040005002:0680aaaaaaaaaa";
			Batchable decoder = new BatchObdCommand("", commands);

			Map<ObdCommand, String> values = decoder.decode(message);
			
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("03")), "41030000");
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("04")), "410400");
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("05")), "410500");
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("06")), "410680");

		}
	}
	
	@Test
	public void t1() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("mode01.json")) {

			PidRegistry registry = PidRegistry.builder().source(source).build();
			List<ObdCommand> commands = new ArrayList<>();
			commands.add(new ObdCommand(registry.findBy("05")));
			commands.add(new ObdCommand(registry.findBy("0B")));
			commands.add(new ObdCommand(registry.findBy("0C")));
			commands.add(new ObdCommand(registry.findBy("0F")));
			commands.add(new ObdCommand(registry.findBy("11")));
			commands.add(new ObdCommand(registry.findBy("0D")));
			String message ="00c0:4105000bff0c1:00000f001100aa";
			Batchable decoder = new BatchObdCommand("", commands);

			Map<ObdCommand, String> values = decoder.decode(message);
			
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("05")), "410500");
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0B")), "410Bff");
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")), "410C1000");
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0F")), "410F00");
			Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("11")), "411100");
		}
	}
}
