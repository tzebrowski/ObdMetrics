package org.openobd2.core.converter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.converter.DynamicFormulaEvaluator;
import org.openobd2.core.definition.PidDefinitionRegistry;

public class VehicleSpeedCommandTest {
	@Test
	public void positiveTest() {
		final String definitionFile = "definitions.json";
		final PidDefinitionRegistry pidRegistry = PidDefinitionRegistry.builder().definitionFile(definitionFile).build();
		final DynamicFormulaEvaluator converterEngine = DynamicFormulaEvaluator.builder().definitionsRegistry(pidRegistry).build();

		String rawData = "410D3F";
		Integer temp = converterEngine.convert(rawData, Integer.class);
		Assertions.assertThat(temp).isEqualTo(63);

		rawData = "410d00";
		temp = converterEngine.convert(rawData, Integer.class);
		Assertions.assertThat(temp).isEqualTo(0);
	}
}
