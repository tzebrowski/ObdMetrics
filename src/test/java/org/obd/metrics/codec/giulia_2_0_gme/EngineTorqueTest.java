package org.obd.metrics.codec.giulia_2_0_gme;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class EngineTorqueTest implements Giulia_2_0_GME_Test {
	
	@ParameterizedTest
	@CsvSource(value = { 
			"62101801F7=50",
			}, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertCloseTo(input, Float.parseFloat(expected),5f);
	}
}
