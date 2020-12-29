package org.openobd2.core.codec;

import java.io.IOException;
import java.io.InputStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.FormulaEvaluator;
import org.openobd2.core.pid.PidRegistry;

public class IgnitationAngleReductionTest {
	@Test
	public void cylinder1() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("alfa.json")) {

			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

			FormulaEvaluator converterEngine = FormulaEvaluator.builder().pids(pidRegistry).build();

			String rawData = "62186C00";
			Object temp = converterEngine.decode(rawData);
			Assertions.assertThat(temp).isEqualTo(0.0); // wrong scaling factor
		}
	}
}
