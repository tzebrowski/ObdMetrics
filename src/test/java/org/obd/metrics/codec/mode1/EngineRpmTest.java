package org.obd.metrics.codec.mode1;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class EngineRpmTest implements Mode01Test {
	
	@ParameterizedTest
	@CsvSource(value = { "410C541B=5382", "410C1000=1024" }, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertEquals(input, Integer.parseInt(expected));
	}
}
