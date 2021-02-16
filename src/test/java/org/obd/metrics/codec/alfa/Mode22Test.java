package org.obd.metrics.codec.alfa;

import org.obd.metrics.codec.CodecTest;

public interface Mode22Test extends CodecTest {

	default void assertThat(String actualValue, Object expectedValue) {
		codecTest(actualValue.substring(2, 6), "alfa.json", actualValue, expectedValue);
	}
}
