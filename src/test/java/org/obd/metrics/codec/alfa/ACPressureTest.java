package org.obd.metrics.codec.alfa;

import org.junit.jupiter.api.Test;

public class ACPressureTest implements Mode22Test {
	@Test
	public void case1(){
		assertEquals("62192F24", 19.0);
	}
}
