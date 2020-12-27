package org.openobd2.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.command.obd.mode1.SupportedPidsCommand;
import org.openobd2.core.converter.Converter;
import org.openobd2.core.converter.ConvertersRegistry;
import org.openobd2.core.pid.PidDefinitionRegistry;

@SuppressWarnings("unchecked")
public class SupportedPidsCommandTest {
	
	@Test
	public void positiveTest() throws IOException {
		
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("generic.json")) {

			final PidDefinitionRegistry pidRegistry = PidDefinitionRegistry.builder().source(source).build();

			final String pids = "4100BE3E2F00";
			final Optional<Converter<?>> findConverter = ConvertersRegistry.builder().pidRegistry(pidRegistry).build()
					.findConverter(new SupportedPidsCommand("00"));
			final Converter<?> converter = findConverter.get();
			final List<String> supportedPids = (List<String>) converter.convert(pids);

			Assertions.assertThat(supportedPids).isNotNull().isNotEmpty().containsExactly("01", "03", "04", "05", "06",
					"07", "0b", "0c", "0d", "0e", "0f", "13", "15", "16", "17", "18");

		}
	}
}
