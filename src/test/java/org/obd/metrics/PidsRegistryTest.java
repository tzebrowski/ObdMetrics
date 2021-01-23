package org.obd.metrics;

import java.io.IOException;
import java.io.InputStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidRegistry;

public class PidsRegistryTest {
	@Test
	public void findByModeAndPidTest() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("mode01.json")) {

			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();
			String rawData = "410c541B";

			PidDefinition findByAnswerRawData = pidRegistry.findByAnswerRawData(rawData);
			Assertions.assertThat(findByAnswerRawData).isNotNull();

			PidDefinition findBy = pidRegistry.findBy("01", "0c");
			Assertions.assertThat(findBy).isNotNull().isEqualTo(findByAnswerRawData);
			
			findBy = pidRegistry.findBy("0c");
			Assertions.assertThat(findBy).isNotNull().isEqualTo(findByAnswerRawData);
		}
	}
}
