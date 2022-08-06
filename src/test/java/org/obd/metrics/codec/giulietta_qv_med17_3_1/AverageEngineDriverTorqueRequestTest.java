package org.obd.metrics.codec.giulietta_qv_med17_3_1;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class AverageEngineDriverTorqueRequestTest implements Giulietta_QV_Med_17_3_1_Test {
	@ParameterizedTest
	@CsvSource(value = { 
			"6218AE0E3A=5.36",
			"6218AE1830=9.37",	
			}, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertCloseTo(input, Float.parseFloat(expected),4.0f);
	}
}
