package org.obd.metrics.codec.giulia_2_0_gme;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class FuelConsumptionTest implements Giulia_2_0_GME_Test {

	@ParameterizedTest
	@CsvSource(value = { 
			"62194200=0.0",
			"62194216=2.2",
			}, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertEquals(Boolean.TRUE, input, Double.parseDouble(expected));
	}
}
