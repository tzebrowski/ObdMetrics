package org.openobd2.core.converter;

import java.net.URL;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.converter.FormulaEvaluator;
import org.openobd2.core.pid.PidDefinitionRegistry;

public class VehicleSpeedCommandTest {
	@Test
	public void positiveTest() {
		final URL fileUrl = Thread.currentThread().getContextClassLoader()
				.getResource("definitions.json");

		final PidDefinitionRegistry pidRegistry = PidDefinitionRegistry.builder().source(fileUrl).build();

		final FormulaEvaluator converterEngine = FormulaEvaluator.builder().definitionsRegistry(pidRegistry).build();

		String rawData = "410D3F";
		Integer temp = converterEngine.convert(rawData, Integer.class);
		Assertions.assertThat(temp).isEqualTo(63);

		rawData = "410d00";
		temp = converterEngine.convert(rawData, Integer.class);
		Assertions.assertThat(temp).isEqualTo(0);
	}
}
