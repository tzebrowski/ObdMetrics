package org.openobd2.core.converter;

import java.net.URL;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.converter.FormulaEvaluator;
import org.openobd2.core.pid.PidDefinitionRegistry;

public class EngineRpmCommandTest {
	@Test
	public void possitiveTest() {
		final URL fileUrl = Thread.currentThread().getContextClassLoader()
				.getResource("definitions.json");

		final PidDefinitionRegistry pidRegistry = PidDefinitionRegistry.builder().source(fileUrl).build();

		FormulaEvaluator converterEngine = FormulaEvaluator.builder().definitionsRegistry(pidRegistry).build();

		String rawData = "410c541B";
		Object temp = converterEngine.convert(rawData);
		Assertions.assertThat(temp).isEqualTo(5382.75);

	}
}
