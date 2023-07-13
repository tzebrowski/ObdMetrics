package org.obd.metrics.codec.giulia_2_0_gme;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class IntakeTempTest implements Giulia_2_0_GME_Test {

	@ParameterizedTest
	@CsvSource(value = { 
			"6219355E=54",
			"62193558=48"
			}, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertEquals(input, Integer.parseInt(expected));
	}
}
