package org.obd.metrics.codec.mode1;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class O2_SensorVoltageTest implements Mode01Test {
	
	@Test
	public void lean_mixture() {
		assertEquals("41175aff", 0.45);
	}
	
	@Test
	public void reach_mixture() throws IOException {
		
		assertEquals("4117b4ff", 0.9);
	}
}
