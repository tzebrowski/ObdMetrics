package org.obd.metrics.codec.alfa;

import org.junit.jupiter.api.Test;

public class EngineRpmTest implements Mode22Test {

	@Test
	public void case1() {
		assertEquals("6210000000", 0.0);
	}

	@Test
	public void case2() {
		assertEquals("6210000BEA", 762.5);
	}
}
