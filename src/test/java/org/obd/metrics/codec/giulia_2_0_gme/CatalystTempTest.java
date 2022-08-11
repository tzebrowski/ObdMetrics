package org.obd.metrics.codec.giulia_2_0_gme;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@Disabled
public class CatalystTempTest implements Giulia_2_0_GME_Test {
	
	@ParameterizedTest
	@CsvSource(value = { 
			"6218375A=400",
			}, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertEquals(input, Float.parseFloat(expected));
	}
}
