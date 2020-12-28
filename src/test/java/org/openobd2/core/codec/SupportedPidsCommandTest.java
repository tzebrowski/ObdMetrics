package org.openobd2.core.codec;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.command.obd.SupportedPidsCommand;
import org.openobd2.core.pid.PidRegistry;

@SuppressWarnings("unchecked")
public class SupportedPidsCommandTest {
	
	
	@Test
	public void pids00() throws IOException {
		
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("generic.json")) {

			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

			final String pids = "4100BE3E2F00";
			final Optional<Codec<?>> findConverter = CodecRegistry.builder().pidRegistry(pidRegistry).build()
					.findCodec(new SupportedPidsCommand("00"));
			final Codec<?> converter = findConverter.get();
			final List<String> supportedPids = (List<String>) converter.decode(pids);

			Assertions.assertThat(supportedPids).isNotNull().isNotEmpty().containsExactly("01", "03", "04", "05", "06",
					"07", "0b", "0c", "0d", "0e", "0f", "13", "15", "16", "17", "18");

		}
	}
	
	@Test
	public void pids20() throws IOException {
		
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("generic.json")) {

			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

			final String pids = "4120a0001000";
			final Optional<Codec<?>> findConverter = CodecRegistry.builder().pidRegistry(pidRegistry).build()
					.findCodec(new SupportedPidsCommand("20"));
			final Codec<?> converter = findConverter.get();
			final List<String> supportedPids = (List<String>) converter.decode(pids);

			Assertions.assertThat(supportedPids).isNotNull().isNotEmpty().containsExactly("01", "03", "14");

		}
	}
}
