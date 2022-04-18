package org.obd.metrics.codec.mode1;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class IntakeTempTest implements Mode01Test {

	@ParameterizedTest
	@CsvSource(value = { "410F2B=3", "410F2F=7" }, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertEquals(input, Integer.parseInt(expected));
	}
}
