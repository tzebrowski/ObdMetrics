package org.openobd2.core.codec.mode1;

import java.io.IOException;
import java.io.InputStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.Codec;
import org.openobd2.core.codec.CodecRegistry;
import org.openobd2.core.command.obd.ObdCommand;
import org.openobd2.core.pid.PidDefinition;
import org.openobd2.core.pid.PidRegistry;

public class EngineRpmCommandTest {
	@Test
	public void possitiveTest() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("generic.json")) {

			PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

			final CodecRegistry codecRegistry = CodecRegistry.builder().pids(pidRegistry).build();
			final Codec<?> codec = codecRegistry.findCodec(new ObdCommand(pidRegistry.findBy("01", "0c"))).get();

			String rawData = "410c541B";

			PidDefinition findByAnswerRawData = pidRegistry.findByAnswerRawData(rawData);
			Assertions.assertThat(findByAnswerRawData).isNotNull();

			Object temp = codec.decode(rawData);
			Assertions.assertThat(temp).isEqualTo(5382.75);
		}

	}
}
