package org.openobd2.core.converter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.converter.DynamicFormulaEvaluator;

public class VehicleSpeedCommandTest {
	@Test
	public void positiveTest() {
		final String definitionFile = "definitions.json";

		DynamicFormulaEvaluator converterEngine = DynamicFormulaEvaluator.builder().definitionFile(definitionFile).build();

		String rawData = "410D3F";
		Integer temp = converterEngine.convert(rawData, Integer.class);
		Assertions.assertThat(temp).isEqualTo(63);

		rawData = "410d00";
		temp = converterEngine.convert(rawData, Integer.class);
		Assertions.assertThat(temp).isEqualTo(0);
	}
}
