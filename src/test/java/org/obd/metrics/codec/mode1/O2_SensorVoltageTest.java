package org.obd.metrics.codec.mode1;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class O2_SensorVoltageTest implements Mode01Test {

	@ParameterizedTest
	@CsvSource(value = { "41175aff=0.45", "4117b4ff=0.9" }, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertEquals(input, Double.parseDouble(expected));
	}
}
