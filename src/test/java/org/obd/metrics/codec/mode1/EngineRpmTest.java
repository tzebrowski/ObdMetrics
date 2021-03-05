package org.obd.metrics.codec.mode1;

import org.junit.jupiter.api.Test;

public class EngineRpmTest implements Mode01Test {
	@Test
	public void case1() {
		assertEquals("410c541B", 5382);
	}

	@Test
	public void case2() {
		assertEquals("410C1000", 1024);
	}
}
