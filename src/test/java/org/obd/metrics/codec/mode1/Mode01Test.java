package org.obd.metrics.codec.mode1;

import org.obd.metrics.codec.PidTest;

public interface Mode01Test extends PidTest {

	default void assertThat(String actual, Object expectedValue) {
		modeTest(actual.substring(2, 4), "mode01.json", actual, expectedValue);
	}
}
