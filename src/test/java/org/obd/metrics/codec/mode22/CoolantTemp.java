package org.obd.metrics.codec.mode22;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class CoolantTemp implements Mode22Test {

	@ParameterizedTest
	@CsvSource(value = { "62100343=1.0",
	        "62100345=3.0", "62100353=13.0",
	        "62100340=-1.0", "621003AB=80.0",
	        "621003AA=79.0", "621003C0=96.0" }, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertEquals(input, Double.parseDouble(expected));
	}
}
