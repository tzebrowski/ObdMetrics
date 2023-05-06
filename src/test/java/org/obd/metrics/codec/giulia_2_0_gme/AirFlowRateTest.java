package org.obd.metrics.codec.giulia_2_0_gme;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class AirFlowRateTest implements Giulia_2_0_GME_Test {

	@ParameterizedTest
	@CsvSource(value = { 
			"62182F0000=0.0",
			"62182F0055=10.53",
			}, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertCloseTo(input,Float.parseFloat(expected), 0.5f);
	}
}
