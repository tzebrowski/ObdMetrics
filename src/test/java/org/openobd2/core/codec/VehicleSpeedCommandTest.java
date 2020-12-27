package org.openobd2.core.codec;

import java.io.IOException;
import java.io.InputStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.FormulaEvaluator;
import org.openobd2.core.pid.PidRegistry;

public class VehicleSpeedCommandTest {
	@Test
	public void positiveTest() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("generic.json")) {

			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

			final FormulaEvaluator formulaEvaluator = FormulaEvaluator.builder().definitionsRegistry(pidRegistry)
					.build();

			String rawData = "410D3F";
			Integer temp = formulaEvaluator.convert(rawData, Integer.class);
			Assertions.assertThat(temp).isEqualTo(63);

			rawData = "410d00";
			temp = formulaEvaluator.convert(rawData, Integer.class);
			Assertions.assertThat(temp).isEqualTo(0);

		}
	}
}