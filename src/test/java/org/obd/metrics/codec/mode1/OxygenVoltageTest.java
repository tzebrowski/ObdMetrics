package org.obd.metrics.codec.mode1;

import org.junit.jupiter.api.Test;

public class OxygenVoltageTest implements Mode01Test {

	@Test
	public void case1() {
		assertEquals("41175aff", 0.45);
	}
}
