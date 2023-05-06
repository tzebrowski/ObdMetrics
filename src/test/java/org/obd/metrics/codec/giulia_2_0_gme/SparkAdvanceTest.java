package org.obd.metrics.codec.giulia_2_0_gme;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class SparkAdvanceTest implements Giulia_2_0_GME_Test {

	@ParameterizedTest
	@CsvSource(value = { 
			"621812004B=5.8",
			}, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertCloseTo(input, Float.parseFloat(expected),0.5f);
	}
}
