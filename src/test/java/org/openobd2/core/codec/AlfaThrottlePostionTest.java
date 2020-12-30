package org.openobd2.core.codec;

import java.io.IOException;
import java.io.InputStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.FormulaEvaluator;
import org.openobd2.core.pid.PidRegistry;

public class AlfaThrottlePostionTest {
	@Test
	public void possitiveTest() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("alfa.json")) {

			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

			FormulaEvaluator formulaEvaluator = FormulaEvaluator.builder().pids(pidRegistry).build();

			String rawData = "6218670000";
			Object temp = formulaEvaluator.decode(rawData);
			Assertions.assertThat(temp).isEqualTo(0.0); // wrong scaling factor
		}
	}
}
