package org.openobd2.core.codec;

import java.io.IOException;
import java.io.InputStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.FormulaEvaluator;
import org.openobd2.core.pid.PidRegistry;

public class EngineTempCommandTest {
	

	@Test
	public void possitiveTest() throws IOException {

		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("generic.json")) {

			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

			FormulaEvaluator converterEngine = FormulaEvaluator.builder().definitionsRegistry(pidRegistry).build();

			String rawData = "410522";
			Object temp = converterEngine.decode(rawData);
			Assertions.assertThat(temp).isEqualTo(-6.0);

			rawData = "410517";
			temp = converterEngine.decode(rawData);
			Assertions.assertThat(temp).isEqualTo(-17.0);
		}
	}
}
