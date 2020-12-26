package org.openobd2.core.converter;

import java.io.IOException;
import java.io.InputStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.pid.PidDefinitionRegistry;

public class VehicleSpeedCommandTest {
	@Test
	public void positiveTest() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("definitions.json")) {

			final PidDefinitionRegistry pidRegistry = PidDefinitionRegistry.builder().source(source).build();

			final FormulaEvaluator converterEngine = FormulaEvaluator.builder().definitionsRegistry(pidRegistry)
					.build();

			String rawData = "410D3F";
			Integer temp = converterEngine.convert(rawData, Integer.class);
			Assertions.assertThat(temp).isEqualTo(63);

			rawData = "410d00";
			temp = converterEngine.convert(rawData, Integer.class);
			Assertions.assertThat(temp).isEqualTo(0);

		}
	}
}
