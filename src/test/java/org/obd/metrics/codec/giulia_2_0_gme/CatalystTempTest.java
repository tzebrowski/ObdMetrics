package org.obd.metrics.codec.giulia_2_0_gme;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class CatalystTempTest implements Giulia_2_0_GME_Test {
	
	@ParameterizedTest
	@CsvSource(value = { 
			"6218375A=400",
			"62183754=370"
			}, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertCloseTo(input, Float.parseFloat(expected),5f);
	}
}
