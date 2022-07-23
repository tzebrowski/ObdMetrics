package org.obd.metrics.codec.giulietta_qv_med17_3_1;

import org.obd.metrics.codec.CodecTest;

public interface Giulietta_QV_Med_17_3_1_Test extends CodecTest {

	default void assertEquals(boolean debug,String actualValue, Object expectedValue) {
		assertEquals(debug, actualValue.substring(2, 6), "alfa.json", actualValue, expectedValue);
	}
	
	default void assertEquals(String actualValue, Object expectedValue) {
		assertEquals(actualValue.substring(2, 6), "alfa.json", actualValue, expectedValue);
	}
	
	default void assertCloseTo(String actualValue, float expectedValue, float offset) {
		assertCloseTo(false, actualValue.substring(2, 6), "alfa.json", actualValue, expectedValue,offset);
	}
}
