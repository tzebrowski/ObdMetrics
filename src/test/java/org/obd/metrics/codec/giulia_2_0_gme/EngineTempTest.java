package org.obd.metrics.codec.giulia_2_0_gme;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class EngineTempTest implements Giulia_2_0_GME_Test {

	@ParameterizedTest
	@CsvSource(value = { 
			"62100336=14.0",
			}, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertEquals(Boolean.TRUE, input, Double.parseDouble(expected));
	}
}
