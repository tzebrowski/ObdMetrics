package org.obd.metrics.codec.giulia_2_0_gme;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class DaysToNextServiceTest implements Giulia_2_0_GME_Test {

	@ParameterizedTest
	@CsvSource(
			value = { 
				"6228070078=120",
			},
			delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertEquals(input, Integer.parseInt(expected));
	}
}
