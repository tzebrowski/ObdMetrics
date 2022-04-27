package org.obd.metrics.codec.mode22;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class MeasuredAirMassFlowTest implements Mode22Test {

	@ParameterizedTest
	@CsvSource(value = { 
			"62180E0059=8.9",
			"62180E0069=10.5", 
			"62180E015C=34.8" }, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertEquals(input, Double.parseDouble(expected));
	}

}
