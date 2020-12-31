package org.openobd2.core.codec.alfa;

import java.io.IOException;
import java.io.InputStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.Codec;
import org.openobd2.core.codec.CodecRegistry;
import org.openobd2.core.command.obd.ObdCommand;
import org.openobd2.core.pid.PidRegistry;

public class EngineTemp {
	@Test
	public void t1() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("alfa.json")) {

			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

			final CodecRegistry codecRegistry = CodecRegistry.builder().pids(pidRegistry).build();
			final Codec<?> codec = codecRegistry.findCodec(new ObdCommand(pidRegistry.findBy("22", "1003"))).get();

			String rawData = "62100340";
			Object temp = codec.decode(rawData);
			Assertions.assertThat(temp).isEqualTo(0.0); // ??
		}
	}

	@Test
	public void t2() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("alfa.json")) {

			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

			final CodecRegistry codecRegistry = CodecRegistry.builder().pids(pidRegistry).build();
			final Codec<?> codec = codecRegistry.findCodec(new ObdCommand(pidRegistry.findBy("22", "1003"))).get();

			String rawData = "621003AB"; // 80.25
			Object temp = codec.decode(rawData);
			Assertions.assertThat(temp).isEqualTo(80.25); // ??
		}
	}

	@Test
	public void t3() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("alfa.json")) {

			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

			final CodecRegistry codecRegistry = CodecRegistry.builder().pids(pidRegistry).build();
			final Codec<?> codec = codecRegistry.findCodec(new ObdCommand(pidRegistry.findBy("22", "1003"))).get();

			String rawData = "621003AA"; // 79.50
			Object temp = codec.decode(rawData);
			Assertions.assertThat(temp).isEqualTo(79.50); // ??
		}
	}

	@Test
	public void t4() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("alfa.json")) {

			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

			final CodecRegistry codecRegistry = CodecRegistry.builder().pids(pidRegistry).build();
			final Codec<?> codec = codecRegistry.findCodec(new ObdCommand(pidRegistry.findBy("22", "1003"))).get();

			String rawData = "621003C0"; // 96
			Object temp = codec.decode(rawData);
			Assertions.assertThat(temp).isEqualTo(96); // ??
		}
	}

}
