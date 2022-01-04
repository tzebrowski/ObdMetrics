package org.obd.metrics.codec.mode1;

import org.junit.jupiter.api.Test;

public class LongFuelTrimTest implements Mode01Test {
	@Test
	public void case_01() {
		assertEquals("410781", 0.78);
	}
}
