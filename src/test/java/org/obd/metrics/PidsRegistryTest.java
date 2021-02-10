package org.obd.metrics;

import java.io.IOException;
import java.io.InputStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.codec.MetricsDecoder;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidRegistry;

public class PidsRegistryTest {
	@Test
	public void findByModeAndPidTest() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("mode01.json")) {

			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();
			
			PidDefinition findBy = pidRegistry.findBy("0c");
			Assertions.assertThat(findBy).isNotNull();
		}
	}
}
