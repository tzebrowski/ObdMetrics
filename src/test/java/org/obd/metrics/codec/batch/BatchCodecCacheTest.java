package org.obd.metrics.codec.batch;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.pid.Resource;
import org.obd.metrics.raw.RawMessage;

public class BatchCodecCacheTest {

	@Test
	public void cacheHitTest() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
		        .getResourceAsStream("mode01.json")) {

			PidDefinitionRegistry registry = PidDefinitionRegistry.builder().source(
					Resource.builder().inputStream(source).name("mode01.json").build()).build();
			
			List<ObdCommand> commands = new ArrayList<>();
			commands.add(new ObdCommand(registry.findBy("0C")));
			commands.add(new ObdCommand(registry.findBy("10")));
			commands.add(new ObdCommand(registry.findBy("0B")));
			commands.add(new ObdCommand(registry.findBy("0D")));
			commands.add(new ObdCommand(registry.findBy("05")));
			final String message = "00B0:410C000010001:000B660D000000";
			final BatchCodec codec = BatchCodec.instance(Adjustments.DEFAULT, message, commands);
			
			

			int len = 10;
			for (int i = 0; i < len; i++) {
				final Map<ObdCommand, RawMessage> values = codec.decode(null, RawMessage.wrap(message.getBytes()));
				Assertions.assertThat(values).containsKey(new ObdCommand(registry.findBy("0B")));
				Assertions.assertThat(values).containsKey(new ObdCommand(registry.findBy("0C")));
				Assertions.assertThat(values).containsKey(new ObdCommand(registry.findBy("0D")));
				Assertions.assertThat(values).containsKey(new ObdCommand(registry.findBy("10")));
			}

			Assertions.assertThat(codec.getCacheHit(message)).isEqualTo(len - 1);
		}
	}
}
