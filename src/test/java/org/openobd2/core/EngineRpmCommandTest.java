package org.openobd2.core;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.converter.ConverterEngine;

public class EngineRpmCommandTest {
	@Test
	public void possitiveTest() {

		final String definitionFile = "definitions.json";

		ConverterEngine converterEngine = ConverterEngine.builder().definitionFile(definitionFile).build();

		String rawData = "410c541B";
		Object temp = converterEngine.convert(rawData);
		Assertions.assertThat(temp).isEqualTo(5382.75);

	}
}
