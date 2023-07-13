package org.obd.metrics.codec.giulietta_qv_med17_3_1;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class TargetOveboostTest implements Giulietta_QV_Med_17_3_1_Test {
//
	@ParameterizedTest
	@CsvSource(value = { "621828214B=673"}, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertEquals(input, Integer.parseInt(expected));
	}
}
