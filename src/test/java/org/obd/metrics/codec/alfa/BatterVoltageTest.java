package org.obd.metrics.codec.alfa;

import org.junit.jupiter.api.Test;

public class BatterVoltageTest implements Mode22Test {
	@Test
	public void case1() {
		assertEquals("62100496", 14.0);
	}

	@Test
	public void case2(){
		assertEquals("62100482", 12.0);
	}

}
