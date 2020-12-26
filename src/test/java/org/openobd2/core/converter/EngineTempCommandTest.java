package org.openobd2.core.converter;

import java.net.URL;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.converter.FormulaEvaluator;
import org.openobd2.core.pid.PidDefinitionRegistry;

public class EngineTempCommandTest {
	@Test
	public void possitiveTest() {

		final URL fileUrl = Thread.currentThread().getContextClassLoader()
				.getResource("definitions.json");

		final PidDefinitionRegistry pidRegistry = PidDefinitionRegistry.builder().source(fileUrl).build();

		FormulaEvaluator converterEngine = FormulaEvaluator.builder().definitionsRegistry(pidRegistry).build();

		String rawData = "410522";
		Object temp = converterEngine.convert(rawData);
		Assertions.assertThat(temp).isEqualTo(-6.0);

		rawData = "410517";
		temp = converterEngine.convert(rawData);
		Assertions.assertThat(temp).isEqualTo(-17.0);
	}
}
