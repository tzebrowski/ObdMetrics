package org.obd.metrics.codec.mode1;

import org.junit.jupiter.api.Test;

public class EngineCalculatedLoadTest implements Mode01Test {
	@Test
	public void case1() {
		assertEquals("410444", 26.67);
	}
}
