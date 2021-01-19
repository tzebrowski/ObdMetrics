package org.openobd2.core.codec.mode1;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.Codec;
import org.openobd2.core.codec.CodecRegistry;
import org.openobd2.core.command.obd.SupportedPidsCommand;
import org.openobd2.core.pid.PidRegistry;

@SuppressWarnings("unchecked")
public class SupportedPidsTest {
	
	
	@Test
	public void pids00() throws IOException {
		
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("mode01.json")) {

			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

			final String pids = "4100BE3E2F00";
			final CodecRegistry codecRegistry = CodecRegistry.builder().pids(pidRegistry).equationEngine("JavaScript").build();
			final Codec<?> codec = codecRegistry.findCodec(new SupportedPidsCommand("00")).get();
			final List<String> supportedPids = (List<String>) codec.decode(pids);

			Assertions.assertThat(supportedPids).isNotNull().isNotEmpty().containsExactly("01", "03", "04", "05", "06",
					"07", "0b", "0c", "0d", "0e", "0f", "13", "15", "16", "17", "18");

		}
	}
	
	@Test
	public void pids20() throws IOException {
		
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("mode01.json")) {

			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

			final String pids = "4120a0001000";
			final CodecRegistry codecRegistry = CodecRegistry.builder().pids(pidRegistry).equationEngine("JavaScript").build();
			
			final Codec<?> codec = codecRegistry.findCodec(new SupportedPidsCommand("20")).get();
			final List<String> supportedPids = (List<String>) codec.decode(pids);
			Assertions.assertThat(supportedPids).isNotNull().isNotEmpty().containsExactly("01", "03", "14");
		}
	}
}
