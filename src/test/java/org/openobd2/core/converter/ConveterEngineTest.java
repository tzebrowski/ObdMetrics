package org.openobd2.core.converter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.converter.DynamicFormulaEvaluator;
import org.openobd2.core.definition.PidDefinitionRegistry;

public class ConveterEngineTest {
	
	@Test
	public void timingTest() {
		final String definitionFile = "definitions.json";
		final PidDefinitionRegistry pidRegistry = PidDefinitionRegistry.builder().definitionFile(definitionFile).build();
		final DynamicFormulaEvaluator converterEngine = DynamicFormulaEvaluator.builder().definitionsRegistry(pidRegistry).build();

		String rawData = "410e80";
		Object temp = converterEngine.convert(rawData);
		Assertions.assertThat(temp).isEqualTo(0.0);
	}
	
	
	
	@Test
	public void engineTempTest() {

		final String definitionFile = "definitions.json";
		final PidDefinitionRegistry pidRegistry = PidDefinitionRegistry.builder().definitionFile(definitionFile).build();
		final DynamicFormulaEvaluator converterEngine = DynamicFormulaEvaluator.builder().definitionsRegistry(pidRegistry).build();

		String rawData = "410522";
		Object temp = converterEngine.convert(rawData);
		Assertions.assertThat(temp).isEqualTo(-6.0);

		rawData = "410517";
		temp = converterEngine.convert(rawData);
		Assertions.assertThat(temp).isEqualTo(-17.0);
	}

	@Test
	public void engineRpmTest() {

		final String definitionFile = "definitions.json";
		final PidDefinitionRegistry pidRegistry = PidDefinitionRegistry.builder().definitionFile(definitionFile).build();
		final DynamicFormulaEvaluator converterEngine = DynamicFormulaEvaluator.builder().definitionsRegistry(pidRegistry).build();

		String rawData = "410c541B";
		Object temp = converterEngine.convert(rawData);
		Assertions.assertThat(temp).isEqualTo(5382.75);

	}


}
