package org.obd.metrics.codec.giulietta_qv_med17_3_1;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@Disabled
public class IgnitationAngleReductionTest implements Giulietta_QV_Med_17_3_1_Test {

	@ParameterizedTest
	@CsvSource(value = { "62186C00=0.0" }, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertEquals(input, Double.parseDouble(expected));
	}

}
