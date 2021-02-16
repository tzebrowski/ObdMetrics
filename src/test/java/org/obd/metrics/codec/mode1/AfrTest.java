package org.obd.metrics.codec.mode1;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class AfrTest implements Mode01Test {
	@Test
	public void case1() throws IOException {
		assertEquals("41155aff", 10.01);
	}
}
