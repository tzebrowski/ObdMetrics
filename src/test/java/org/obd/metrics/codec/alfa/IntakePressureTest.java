package org.obd.metrics.codec.alfa;

import org.junit.jupiter.api.Test;

public class IntakePressureTest implements Mode22Test {

	@Test
	public void case1() {
		assertEquals("62193732b4", 1025);
	}

	@Test
	public void case2() {
		assertEquals("62193731E7", 1009);
	}

	@Test
	public void case3() {
		assertEquals("6219373307", 1031);
	}
}
