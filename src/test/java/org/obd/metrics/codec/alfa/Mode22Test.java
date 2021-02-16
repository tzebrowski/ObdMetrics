package org.obd.metrics.codec.alfa;

import org.obd.metrics.codec.PidTest;

public interface Mode22Test extends PidTest {

	default void assertThat(String actual, Object expectedValue) {
		modeTest(actual.substring(2, 6), "alfa.json", actual, expectedValue);
	}
}
