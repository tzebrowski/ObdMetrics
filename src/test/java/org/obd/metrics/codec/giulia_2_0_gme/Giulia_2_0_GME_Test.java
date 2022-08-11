package org.obd.metrics.codec.giulia_2_0_gme;

import org.obd.metrics.codec.CodecTest;

public interface Giulia_2_0_GME_Test extends CodecTest {

	public static final String PID_FILE = "giulia_2.0_gme.json";

	default void assertEquals(String actualValue, Object expectedValue) {
		assertEquals(Boolean.FALSE, actualValue, expectedValue);
	}


	default void assertCloseTo(String actualValue, float expectedValue, float offset) {
		assertCloseTo(false, actualValue.substring(2, 6), PID_FILE, actualValue, expectedValue, offset);
	}
	
	default void assertEquals(boolean debug, String actualValue, Object expectedValue) {
		assertEquals(debug, actualValue.substring(2, 6), null, PID_FILE, actualValue, expectedValue);
	}
}
