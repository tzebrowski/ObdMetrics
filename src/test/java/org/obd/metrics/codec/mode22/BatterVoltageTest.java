package org.obd.metrics.codec.mode22;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class BatterVoltageTest implements Mode22Test {
	
	@ParameterizedTest
	@CsvSource(value = { "62100496=14.0", "62100482=12.0" }, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertEquals(input, Double.parseDouble(expected));
	}
}
