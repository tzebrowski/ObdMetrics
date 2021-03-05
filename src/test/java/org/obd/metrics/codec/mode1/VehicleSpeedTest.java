package org.obd.metrics.codec.mode1;

import org.junit.jupiter.api.Test;

public class VehicleSpeedTest implements Mode01Test {
	@Test
	public void case1() {
		assertEquals("410D3F", 63);
	}

	@Test
	public void case2() {
		assertEquals("410d00", 0);
	}
}
