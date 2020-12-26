package org.openobd2.core.converter;

import java.net.URL;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.pid.PidDefinitionRegistry;

public class FormulaEvaluatorTest {

	@Test
	public void timingTest() {
		final URL fileUrl = Thread.currentThread().getContextClassLoader().getResource("definitions.json");

		final PidDefinitionRegistry pidRegistry = PidDefinitionRegistry.builder().source(fileUrl).build();

		final FormulaEvaluator converterEngine = FormulaEvaluator.builder().definitionsRegistry(pidRegistry).build();

		String rawData = "410e80";
		Object temp = converterEngine.convert(rawData);
		Assertions.assertThat(temp).isEqualTo(0.0);
	}

	@Test
	public void engineTempTest() {

		final URL fileUrl = Thread.currentThread().getContextClassLoader().getResource("definitions.json");

		final PidDefinitionRegistry pidRegistry = PidDefinitionRegistry.builder().source(fileUrl).build();
		final FormulaEvaluator converterEngine = FormulaEvaluator.builder().definitionsRegistry(pidRegistry).build();

		String rawData = "410522";
		Object temp = converterEngine.convert(rawData);
		Assertions.assertThat(temp).isEqualTo(-6.0);

		rawData = "410517";
		temp = converterEngine.convert(rawData);
		Assertions.assertThat(temp).isEqualTo(-17.0);
	}

	@Test
	public void engineRpmTest() {

		final URL fileUrl = Thread.currentThread().getContextClassLoader().getResource("definitions.json");

		final PidDefinitionRegistry pidRegistry = PidDefinitionRegistry.builder().source(fileUrl).build();

		final FormulaEvaluator converterEngine = FormulaEvaluator.builder().definitionsRegistry(pidRegistry).build();

		String rawData = "410c541B";
		Object temp = converterEngine.convert(rawData);
		Assertions.assertThat(temp).isEqualTo(5382.75);

	}

}
