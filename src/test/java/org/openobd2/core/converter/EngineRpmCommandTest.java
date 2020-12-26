package org.openobd2.core.converter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.converter.DynamicFormulaEvaluator;
import org.openobd2.core.definition.PidDefinitionRegistry;

public class EngineRpmCommandTest {
	@Test
	public void possitiveTest() {
		final String definitionFile = "definitions.json";
		final PidDefinitionRegistry pidRegistry = PidDefinitionRegistry.builder().definitionFile(definitionFile).build();
		DynamicFormulaEvaluator converterEngine = DynamicFormulaEvaluator.builder().definitionsRegistry(pidRegistry).build();

		String rawData = "410c541B";
		Object temp = converterEngine.convert(rawData);
		Assertions.assertThat(temp).isEqualTo(5382.75);

	}
}
