package org.obd.metrics.codec.alfa;

import org.junit.jupiter.api.Test;

public class TargetAirMassFlowTest implements Mode22Test {
	@Test
	public void case1()  {
		assertEquals("6218670059", 8.9);
	}
}
