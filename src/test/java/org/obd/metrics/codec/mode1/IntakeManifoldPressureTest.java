package org.obd.metrics.codec.mode1;

import org.junit.jupiter.api.Test;

public class IntakeManifoldPressureTest implements Mode01Test {
	@Test
	public void case1() {
		assertEquals("410b1e", -0.7);
	}

	@Test
	public void case2() {
		assertEquals("410b35", -0.47);
	}

	@Test
	public void case3() {
		assertEquals("410b62", -0.02);
	}

}
