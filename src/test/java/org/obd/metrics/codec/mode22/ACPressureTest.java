package org.obd.metrics.codec.mode22;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@Disabled
public class ACPressureTest implements Mode22Test {

	@ParameterizedTest
	@CsvSource(value = { "62192F24=19.0" }, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertEquals(input, Double.parseDouble(expected));
	}
}
