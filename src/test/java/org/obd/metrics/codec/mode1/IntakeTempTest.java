package org.obd.metrics.codec.mode1;

import org.junit.jupiter.api.Test;

public class IntakeTempTest implements Mode01Test {
	@Test
	public void case1() {
		assertEquals("410f2f", 7);
	}
}
