package org.obd.metrics.codec.giulia_2_0_gme;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class OilPressureTest implements Giulia_2_0_GME_Test {

	@ParameterizedTest
	@CsvSource(
			value = { 
				"62130A39=1.28",
				"62130A3B=1.44",
				"62130A6F=3.4"
			},
			delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertCloseTo(input, Float.parseFloat(expected), 0.2f);
	}
}
