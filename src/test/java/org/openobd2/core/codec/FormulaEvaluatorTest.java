package org.openobd2.core.codec;

import java.io.IOException;
import java.io.InputStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.FormulaEvaluator;
import org.openobd2.core.pid.PidRegistry;


// mode 22
public class FormulaEvaluatorTest {

	@Test
	public void timingTest() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("generic.json")) {

			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

			final FormulaEvaluator formulaEvaluator = FormulaEvaluator.builder().pids(pidRegistry)
					.build();

			String rawData = "410e80";
			Object temp = formulaEvaluator.decode(rawData);
			Assertions.assertThat(temp).isEqualTo(0.0);
		}
	}

	@Test
	public void engineTempTest() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("generic.json")) {

			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();
			final FormulaEvaluator formulaEvaluator = FormulaEvaluator.builder().pids(pidRegistry)
					.build();

			String rawData = "410522";
			Object temp = formulaEvaluator.decode(rawData);
			Assertions.assertThat(temp).isEqualTo(-6.0);

			rawData = "410517";
			temp = formulaEvaluator.decode(rawData);
			Assertions.assertThat(temp).isEqualTo(-17.0);
		}
	}

	@Test
	public void engineRpmTest() throws IOException {

		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("generic.json")) {

			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

			final FormulaEvaluator formulaEvaluator = FormulaEvaluator.builder().pids(pidRegistry)
					.build();

			String rawData = "410c541B";
			Object temp = formulaEvaluator.decode(rawData);
			Assertions.assertThat(temp).isEqualTo(5382.75);
		}
	}
}
