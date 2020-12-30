package org.openobd2.core.codec;

import java.io.IOException;
import java.io.InputStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.pid.PidRegistry;

public class AlaEngineRpmTest {
	@Test
	public void targetRpmTest() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("alfa.json")) {

			PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

			FormulaEvaluator formulaEvaluator = FormulaEvaluator.builder().pids(pidRegistry).build();

			String rawData = "62186B6E";
			Object rpm = formulaEvaluator.decode(rawData);
			Assertions.assertThat(rpm).isEqualTo(1100.0);
		}

	}
	
	
	@Test
	public void rpmTest() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("alfa.json")) {

			PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

			FormulaEvaluator formulaEvaluator = FormulaEvaluator.builder().pids(pidRegistry).build();

			String rawData = "6210000000";
			Object rpm = formulaEvaluator.decode(rawData);
			Assertions.assertThat(rpm).isEqualTo(0.0);
		}

	}
}
