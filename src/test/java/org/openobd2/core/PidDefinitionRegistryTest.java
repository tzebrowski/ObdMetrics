package org.openobd2.core;

import java.io.IOException;
import java.io.InputStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.pid.PidDefinition;
import org.openobd2.core.pid.PidDefinitionRegistry;

public class PidDefinitionRegistryTest {
	@Test
	public void findByModeAndPidTest() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("generic.json")) {

			final PidDefinitionRegistry pidRegistry = PidDefinitionRegistry.builder().source(source).build();
			String rawData = "410c541B";

			PidDefinition findByAnswerRawData = pidRegistry.findByAnswerRawData(rawData);
			Assertions.assertThat(findByAnswerRawData).isNotNull();

			PidDefinition findBy = pidRegistry.findBy("01", "0c");
			Assertions.assertThat(findBy).isNotNull().isEqualTo(findByAnswerRawData);

		}

	}
}
