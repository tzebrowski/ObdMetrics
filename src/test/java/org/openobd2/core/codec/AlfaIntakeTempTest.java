package org.openobd2.core.codec;

import java.io.IOException;
import java.io.InputStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.FormulaEvaluator;
import org.openobd2.core.pid.PidRegistry;

public class AlfaIntakeTempTest {
	@Test
	public void tempTest() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("alfa.json")) {

			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

			FormulaEvaluator formulaEvaluator = FormulaEvaluator.builder().pids(pidRegistry).build();
			String rawData = "62193540";
			Object temp = formulaEvaluator.decode(rawData);
			Assertions.assertThat(temp).isEqualTo(0.0); 
		}
	}
}
