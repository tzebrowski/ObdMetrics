package org.obd.metrics.codec.giulia_2_0_gme;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class TargetIntakeManifoldPressure implements Giulia_2_0_GME_Test {
	
	@ParameterizedTest
	@CsvSource(value = { 
			"62181F0119=281.0",
			"62181F03EB=1003.0",
	}, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertEquals(input, Double.parseDouble(expected));
	}
}
