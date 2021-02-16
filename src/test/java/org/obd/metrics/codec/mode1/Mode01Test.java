package org.obd.metrics.codec.mode1;

import org.obd.metrics.codec.CodecTest;

public interface Mode01Test extends CodecTest {

	default void assertEquals(String actualValue, Object expectedValue) {
		assertEquals(actualValue.substring(2, 4), "mode01.json", actualValue, expectedValue);
	}
}
