package org.obd.metrics.codec.giulia_2_0_gme;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class FuelPressureCalculatedTest implements Giulia_2_0_GME_Test {

	@ParameterizedTest
	@CsvSource(
			value = { 
				"621947209B=49.98",
			},
			delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertEquals(input, Double.parseDouble(expected));
	}
}
