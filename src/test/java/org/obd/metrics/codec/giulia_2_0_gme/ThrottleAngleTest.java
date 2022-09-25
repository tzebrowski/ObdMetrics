package org.obd.metrics.codec.giulia_2_0_gme;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class ThrottleAngleTest implements Giulia_2_0_GME_Test {
	@ParameterizedTest
	@CsvSource(value = { 
			"6218020080=8.0",	
			"621802008D=9.6"
	        }, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertCloseTo(input, Float.parseFloat(expected), 0.5f);
	}
}
