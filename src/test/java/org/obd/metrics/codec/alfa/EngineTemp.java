package org.obd.metrics.codec.alfa;

import org.junit.jupiter.api.Test;

public class EngineTemp implements Mode22Test {
	@Test
	public void case1() {
		assertEquals("62100340", -1);
	}

	@Test
	public void case2() {
		assertEquals("621003AB", 80);
	}

	@Test
	public void case3() {
		assertEquals("621003AA", 79);
	}

	@Test
	public void case4() {
		assertEquals("621003C0", 96);
	}
}
