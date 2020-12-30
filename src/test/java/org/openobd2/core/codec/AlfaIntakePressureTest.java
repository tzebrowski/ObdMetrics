package org.openobd2.core.codec;

import java.io.IOException;
import java.io.InputStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.FormulaEvaluator;
import org.openobd2.core.pid.PidRegistry;

public class AlfaIntakePressureTest {
	@Test
	public void calculatedTest() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("alfa.json")) {

			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

			FormulaEvaluator converterEngine = FormulaEvaluator.builder().pids(pidRegistry).build();
			String rawData = "62193731E7";
			Object temp = converterEngine.decode(rawData);
			Assertions.assertThat(temp).isEqualTo(995.0); 
		}
	}
	
	@Test
	public void targetTest() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("alfa.json")) {

			final PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

			FormulaEvaluator converterEngine = FormulaEvaluator.builder().pids(pidRegistry).build();
			String rawData = "62181F63CE";
			Object temp = converterEngine.decode(rawData);
			Assertions.assertThat(temp).isEqualTo(990.0); 
		}
	}
}
