package org.obd.metrics.codec.mode1;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class IntakeManifoldPressureTest implements Mode01Test {

	@ParameterizedTest
	@CsvSource(value = { "410B1E=  -0.7", "410B35=-0.47", "410B62=-0.02" }, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertEquals(input, Double.parseDouble(expected));
	}
}
