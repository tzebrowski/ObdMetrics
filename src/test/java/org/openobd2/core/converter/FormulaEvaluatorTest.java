package org.openobd2.core.converter;

import java.io.IOException;
import java.io.InputStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.pid.PidRegistry;

public class FormulaEvaluatorTest {

	@Test
	public void timingTest() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("generic.json")) {

			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

			final FormulaEvaluator formulaEvaluator = FormulaEvaluator.builder().definitionsRegistry(pidRegistry)
					.build();

			String rawData = "410e80";
			Object temp = formulaEvaluator.convert(rawData);
			Assertions.assertThat(temp).isEqualTo(0.0);
		}
	}

	@Test
	public void engineTempTest() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("generic.json")) {

			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();
			final FormulaEvaluator converterEngine = FormulaEvaluator.builder().definitionsRegistry(pidRegistry)
					.build();

			String rawData = "410522";
			Object temp = converterEngine.convert(rawData);
			Assertions.assertThat(temp).isEqualTo(-6.0);

			rawData = "410517";
			temp = converterEngine.convert(rawData);
			Assertions.assertThat(temp).isEqualTo(-17.0);
		}
	}

	@Test
	public void engineRpmTest() throws IOException {

		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("generic.json")) {

			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

			final FormulaEvaluator converterEngine = FormulaEvaluator.builder().definitionsRegistry(pidRegistry)
					.build();

			String rawData = "410c541B";
			Object temp = converterEngine.convert(rawData);
			Assertions.assertThat(temp).isEqualTo(5382.75);
		}
	}
}
