package org.obd.metrics.codec.giulia_2_0_gme;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class GearEngagedTest implements Giulia_2_0_GME_Test {

	@ParameterizedTest
	@CsvSource(value = { 
			"62051ADD=0",
			"62051ANN=-1",
			"62051A11=1",
			"62051A22=2",
			"62051A33=3",
			"62051A44=4",
			"62051A55=5",
			"62051A66=6",
			"62051A77=7",
			"62051A88=8",
			
			}, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertEquals(Boolean.TRUE, input, Integer.parseInt(expected));
	}
}
