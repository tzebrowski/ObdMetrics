package org.obd.metrics.codec.alfa;

import org.junit.jupiter.api.Test;

public class OilTempTest implements Mode22Test {
	@Test
	public void case1() {
		assertEquals("62194f2d85", 0.0);
	}

	@Test
	public void case2() {
		assertEquals("62194F3BE5", 113);
	}

	@Test
	public void case3() {
		assertEquals("62194F2D85", -0.027);
	}

	@Test
	public void case4() {
		assertEquals("62194F3B85", 83.97);
	}

	@Test
	public void case5() {
		assertEquals("62194F3E65", 101);
	}

	@Test
	public void case6() {
		assertEquals("62194f2da5", 2.0);
	}
}
