package org.obd.metrics.codec.giulia_2_0_gme;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class BoostPressureTest implements Giulia_2_0_GME_Test {

	@ParameterizedTest
	@CsvSource(value = { 
			"62195A03EC=1004",
			"62195A03E9=1001",
			}, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertEquals(input, Integer.parseInt(expected));
	}
}
