package org.obd.metrics.codec.alfa;

import org.junit.jupiter.api.Test;

public class TargetEngineRpmTest implements Mode22Test {

	@Test
	public void case1() {
		assertEquals("62186B6E", 1100.0);
	}
}
