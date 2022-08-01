package org.obd.metrics.codec.giulietta_qv_med17_3_1;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class CalculatedPowerTest implements Giulietta_QV_Med_17_3_1_Test {

	@ParameterizedTest
	@CsvSource(value = { "62180E115C=164" }, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertEquals(6024l, input, Double.parseDouble(expected));
	}

}
