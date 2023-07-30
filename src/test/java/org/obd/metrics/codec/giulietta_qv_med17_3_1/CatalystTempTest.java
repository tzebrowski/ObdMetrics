package org.obd.metrics.codec.giulietta_qv_med17_3_1;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class CatalystTempTest implements Giulietta_QV_Med_17_3_1_Test {
	@ParameterizedTest
	@CsvSource(value = { 
//			"6218371E=60",
//	        "6218370E=20",
//	        "6218370D=15",
	        "62183743=288",
	        "62183744=292",
	        "62183748=309",
			}, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertEquals(input, Integer.parseInt(expected));
	}
}
