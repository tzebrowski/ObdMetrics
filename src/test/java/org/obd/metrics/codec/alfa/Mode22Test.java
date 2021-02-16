package org.obd.metrics.codec.alfa;

import org.obd.metrics.codec.CodecTest;

public interface Mode22Test extends CodecTest {

	default void assertEquals(String actualValue, Object expectedValue) {
		assertEquals(actualValue.substring(2, 6), "alfa.json", actualValue, expectedValue);
	}
}
