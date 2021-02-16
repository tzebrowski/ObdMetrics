package org.obd.metrics.codec.mode1;

import org.junit.jupiter.api.Test;

public class EngineTempTest implements Mode01Test {

	@Test
	public void case1() {
		assertEquals("410522", -6);
	}

	@Test
	public void case2() {
		assertEquals("410517", -17);
	}
}
