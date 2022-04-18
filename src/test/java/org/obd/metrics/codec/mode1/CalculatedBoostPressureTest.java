package org.obd.metrics.codec.mode1;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class CalculatedBoostPressureTest implements Mode01Test {

	@ParameterizedTest
	@CsvSource(value = { "410BFF=1.55" }, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertEquals(input, Double.parseDouble(expected));
	}
}
