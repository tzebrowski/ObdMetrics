package org.obd.metrics.codec.mode22;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

//(A*0x100 + B) < 0x8000 ? (A*0x100 + B) : (A*0x100 + B - 0x10000)"
@Disabled
public class OilTempTest implements Mode22Test {

	@ParameterizedTest
	@CsvSource(value = {
			"62194F2FA5=11",
		    "62194F2E45=4",
	        "62194F2E25=3",
	        "62194F3B85=83.97",
	        "62194F3BE5= 113.0",
	        "62194F2d85= 0.0",
	        "62194F2D85=-0.027",
	        "62194F3E65=101.0",
	        "62194F2da5=2.0",
	        "62194F2DC5= 1.0" }, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertEquals(input, Double.parseDouble(expected));
	}
}
