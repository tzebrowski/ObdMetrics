package org.obd.metrics.codec.mode22;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class TargetAirMassFlowTest implements Mode22Test {

	@ParameterizedTest
	@CsvSource(value = { "6218670059=8.9" }, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertEquals(input, Double.parseDouble(expected));
	}
}
