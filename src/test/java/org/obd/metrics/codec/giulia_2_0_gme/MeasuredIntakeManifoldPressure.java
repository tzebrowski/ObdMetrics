package org.obd.metrics.codec.giulia_2_0_gme;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class MeasuredIntakeManifoldPressure implements Giulia_2_0_GME_Test {

	@ParameterizedTest
	@CsvSource(value = { 
			"621937011C=284.0",
			"62193703E2=994.0"
			}, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertEquals(input, Double.parseDouble(expected));
	}
}
