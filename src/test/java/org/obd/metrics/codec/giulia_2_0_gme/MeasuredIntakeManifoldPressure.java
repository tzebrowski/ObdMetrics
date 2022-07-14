package org.obd.metrics.codec.giulia_2_0_gme;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class MeasuredIntakeManifoldPressure implements Giulia_2_0_GME_Test {

	//13:05:39.352 TRACE DefaultConnector - TX: 22 1937 181F 180E 2
	//13:05:39.388 TRACE DefaultConnector - RX: 0090:621937011C181:1F0119, processing time: 36ms
	
	@ParameterizedTest
	@CsvSource(value = { 
			"621937011C=284.0",
			}, delimiter = '=')
	public void parameterizedTest(String input, String expected) {
		assertEquals(input, Double.parseDouble(expected));
	}
}
