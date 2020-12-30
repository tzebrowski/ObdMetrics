package org.openobd2.core.codec.alfa;

import java.io.IOException;
import java.io.InputStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.Codec;
import org.openobd2.core.codec.CodecRegistry;
import org.openobd2.core.command.obd.ObdCommand;
import org.openobd2.core.pid.PidRegistry;

public class EngineRpmTest {

	@Test
	public void targetRpmTest() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("alfa.json")) {

			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();
			final CodecRegistry codecRegistry = CodecRegistry.builder().pids(pidRegistry).build();
			final Codec<?> codec = codecRegistry.findCodec(new ObdCommand(pidRegistry.findBy("22", "186B"))).get();

			final String rawData = "62186B6E";
			Object rpm = codec.decode(rawData);
			Assertions.assertThat(rpm).isEqualTo(1100.0);
		}
	}

	@Test
	public void rpmTest() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("alfa.json")) {

			PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

			final CodecRegistry codecRegistry = CodecRegistry.builder().pids(pidRegistry).build();
			final Codec<?> codec = codecRegistry.findCodec(new ObdCommand(pidRegistry.findBy("22", "1000"))).get();

			final String rawData = "6210000000";
			final Object rpm = codec.decode(rawData);
			Assertions.assertThat(rpm).isEqualTo(0.0);
		}
	}
}
