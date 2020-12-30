package org.openobd2.core.codec;

import java.io.IOException;
import java.io.InputStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.FormulaEvaluator;
import org.openobd2.core.pid.PidRegistry;

public class AlfaACPressureTest {
	@Test
	public void cylinder1() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("alfa.json")) {

			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

			FormulaEvaluator formulaEvaluator = FormulaEvaluator.builder().pids(pidRegistry).build();

			String rawData = "62192F24";
			Object temp = formulaEvaluator.decode(rawData);
			Assertions.assertThat(temp).isEqualTo(19.); // wrong scaling factor
		}
	}
}
