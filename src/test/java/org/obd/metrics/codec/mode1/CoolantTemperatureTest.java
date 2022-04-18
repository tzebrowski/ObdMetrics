package org.obd.metrics.codec.mode1;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class CoolantTemperatureTest implements Mode01Test {

	@ParameterizedTest
	@CsvSource(value = { "41052A=2", "410500=-40", "410522=-6", "410517=-17" }, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertEquals(input, Integer.parseInt(expected));
	}
}
