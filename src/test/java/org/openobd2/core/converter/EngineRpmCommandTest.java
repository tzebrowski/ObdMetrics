package org.openobd2.core.converter;

import java.io.IOException;
import java.io.InputStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.pid.PidDefinition;
import org.openobd2.core.pid.PidRegistry;

public class EngineRpmCommandTest {
	@Test
	public void possitiveTest() throws IOException {
		try (final InputStream source = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("generic.json")) {

			PidRegistry pidRegistry = PidRegistry.builder().source(source).build();

			FormulaEvaluator formulaEvaluator = FormulaEvaluator.builder().definitionsRegistry(pidRegistry).build();

			String rawData = "410c541B";
			
			PidDefinition findByAnswerRawData = pidRegistry.findByAnswerRawData(rawData);
			Assertions.assertThat(findByAnswerRawData).isNotNull();

			PidDefinition findBy = pidRegistry.findBy("01","0c");
			Assertions.assertThat(findBy).isNotNull().isEqualTo(findByAnswerRawData);
			
			Object temp = formulaEvaluator.convert(rawData);
			Assertions.assertThat(temp).isEqualTo(5382.75);
		}

	}
}
