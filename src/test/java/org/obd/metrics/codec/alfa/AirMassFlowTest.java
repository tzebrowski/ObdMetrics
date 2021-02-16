package org.obd.metrics.codec.alfa;

import org.junit.jupiter.api.Test;

public class AirMassFlowTest implements Mode22Test {
	@Test
	public void case1() {
		assertEquals("62180E0069", 10.5);
	}

	@Test
	public void case2() {
		assertEquals("62180E015C", 34.8);
	}
}
