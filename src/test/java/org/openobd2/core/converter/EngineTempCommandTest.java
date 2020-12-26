package org.openobd2.core.converter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.converter.DynamicFormulaEvaluator;

public class EngineTempCommandTest {
	@Test
	public void possitiveTest() {

		final String definitionFile = "definitions.json";

		DynamicFormulaEvaluator converterEngine = DynamicFormulaEvaluator.builder().definitionFile(definitionFile).build();

		String rawData = "410522";
		Object temp = converterEngine.convert(rawData);
		Assertions.assertThat(temp).isEqualTo(-6.0);

		rawData = "410517";
		temp = converterEngine.convert(rawData);
		Assertions.assertThat(temp).isEqualTo(-17.0);
	}
}
