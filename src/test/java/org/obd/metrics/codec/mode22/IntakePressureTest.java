package org.obd.metrics.codec.mode22;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


public class IntakePressureTest implements Mode22Test {

	@ParameterizedTest
	@CsvSource(value = { "62193732b4=1025.0", "62193731E7=1009.0","6219373307=1031.0" }, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertEquals(input, Double.parseDouble(expected));
	}
}
