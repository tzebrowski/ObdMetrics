package org.obd.metrics.codec.mode1;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

//http://jimsprojectgarage.weebly.com/edelbrock-carb-tuning-with-a-narrowband-oxygen-sensor.html
public class AFR_Test implements Mode01Test {

	@ParameterizedTest
	@CsvSource(value = { "41155aff=15.12", "4115b4ff=12.61", "41150A14=17.36" }, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertEquals(input, Double.parseDouble(expected));
	}
}
