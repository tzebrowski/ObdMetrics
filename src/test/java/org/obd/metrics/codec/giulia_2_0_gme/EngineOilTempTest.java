package org.obd.metrics.codec.giulia_2_0_gme;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class EngineOilTempTest implements Giulia_2_0_GME_Test {

	@ParameterizedTest
	@CsvSource(value = { 
			"6213020060=96.0",
			"621302003E=62.0",
			"6213020043=67.0",
			"6213020040=64.0",
			}, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertEquals(Boolean.TRUE, input, Double.parseDouble(expected));
	}
}
