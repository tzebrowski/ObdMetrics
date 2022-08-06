package org.obd.metrics.codec.giulietta_qv_med17_3_1;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class ThrottleAngleTest implements Giulietta_QV_Med_17_3_1_Test {
	@ParameterizedTest
	@CsvSource(value = { 
			"621862010A=6.49",	
	        "621862010C=6.643",
			}, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertCloseTo(input, Float.parseFloat(expected), 1.0f);
	}
}
