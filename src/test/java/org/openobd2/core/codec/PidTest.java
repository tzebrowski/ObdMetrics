package org.openobd2.core.codec;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.openobd2.core.codec.Codec;
import org.openobd2.core.codec.CodecRegistry;
import org.openobd2.core.command.obd.ObdCommand;
import org.openobd2.core.pid.PidDefinition;
import org.openobd2.core.pid.PidRegistry;

public interface PidTest {

	default void mode01Test(String rawData, Object expectedValue) throws IOException {

		final String mode = "01";
		final String pid = rawData.substring(2, 4);

		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("generic.json")) {

			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

			final CodecRegistry codecRegistry = CodecRegistry.builder().pids(pidRegistry).build();
			final PidDefinition pidDef = pidRegistry.findBy(mode, pid);
			Assertions.assertThat(pidDef).isNotNull();

			final Optional<Codec<?>> codec = codecRegistry.findCodec(new ObdCommand(pidDef));
			if (codec.isPresent()) {
				final Object value = codec.get().decode(rawData);
				Assertions.assertThat(value).isEqualTo(expectedValue);
			} else {
				Assertions.fail("No codec available for PID: {}", pid);
			}
		}
	}

	default void mode22Test(String rawData, Object expectedValue) throws IOException {

		final String mode = "22";
		final String pid = rawData.substring(2, 6);

		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("alfa.json")) {

			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

			final CodecRegistry codecRegistry = CodecRegistry.builder().pids(pidRegistry).build();
			final PidDefinition pidDef = pidRegistry.findBy(mode, pid);
			Assertions.assertThat(pidDef).isNotNull();

			final Optional<Codec<?>> codec = codecRegistry.findCodec(new ObdCommand(pidDef));
			if (codec.isPresent()) {
				final Object value = codec.get().decode(rawData);
				Assertions.assertThat(value).isEqualTo(expectedValue);
			} else {
				Assertions.fail("No codec available for PID: {}", pid);
			}
		}
	}
}
