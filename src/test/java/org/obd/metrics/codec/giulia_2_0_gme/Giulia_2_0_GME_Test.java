package org.obd.metrics.codec.giulia_2_0_gme;

import org.obd.metrics.codec.CodecTest;

public interface Giulia_2_0_GME_Test extends CodecTest {

	default void assertEquals(String actualValue, Object expectedValue) {
		assertEquals(Boolean.FALSE, actualValue, expectedValue);
	}

	default void assertEquals(boolean debug, String actualValue, Object expectedValue) {
		assertEquals(debug, actualValue.substring(2, 6), "giulia_2.0_gme.json", actualValue, expectedValue);
	}
}
