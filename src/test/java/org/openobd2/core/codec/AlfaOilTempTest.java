package org.openobd2.core.codec;

import java.io.IOException;
import java.io.InputStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.FormulaEvaluator;
import org.openobd2.core.pid.PidRegistry;

public class AlfaOilTempTest {
	@Test
	public void possitiveTest() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("alfa.json")) {

			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

			FormulaEvaluator converterEngine = FormulaEvaluator.builder().pids(pidRegistry).build();
			//62194F2D85
			String rawData = "62194F2D85";//-0.027
			//2D85
			//45 133
			Object temp = converterEngine.decode(rawData);
			Assertions.assertThat(temp).isEqualTo(11781.0); // wrong scaling factor
		}
	}
}
