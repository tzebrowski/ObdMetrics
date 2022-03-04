package org.obd.metrics.batch;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.codec.batch.BatchCodec;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.model.RawMessage;
import org.obd.metrics.pid.PidDefinitionRegistry;

public class BatchCodecCacheTest {

	@Test
	public void cacheHitTest() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
		        .getResourceAsStream("mode01.json")) {

			PidDefinitionRegistry registry = PidDefinitionRegistry.builder().source(source).build();
			List<ObdCommand> commands = new ArrayList<>();
			commands.add(new ObdCommand(registry.findBy("0C")));
			commands.add(new ObdCommand(registry.findBy("10")));
			commands.add(new ObdCommand(registry.findBy("0B")));
			commands.add(new ObdCommand(registry.findBy("0D")));
			commands.add(new ObdCommand(registry.findBy("05")));
			final String message = "00b0:410c000010001:000b660d000000";
			BatchCodec decoder = BatchCodec.instance(message, commands);

			int len = 10;
			for (int i = 0; i < len; i++) {
				final Map<ObdCommand, RawMessage> values = decoder.decode(null, RawMessage.instance(message));
				Assertions.assertThat(values).containsKey(new ObdCommand(registry.findBy("0B")));
				Assertions.assertThat(values).containsKey(new ObdCommand(registry.findBy("0C")));
				Assertions.assertThat(values).containsKey(new ObdCommand(registry.findBy("0D")));
				Assertions.assertThat(values).containsKey(new ObdCommand(registry.findBy("10")));
			}

			Assertions.assertThat(decoder.getCacheHit(message)).isEqualTo(len - 1);
		}
	}
}
