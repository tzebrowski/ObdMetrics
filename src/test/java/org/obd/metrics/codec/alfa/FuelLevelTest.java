package org.obd.metrics.codec.alfa;

import org.junit.jupiter.api.Test;

public class FuelLevelTest implements Mode22Test {
	@Test
	public void case1() {
		assertEquals("62100122", 17.0);
	}
}
