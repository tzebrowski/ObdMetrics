package org.obd.metrics.codec.mode1;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class EngineRpmTest implements Mode01Test {
	@Test
	public void t0() throws IOException {
		assertThat("410c541B", 5382);
	}
	
	
	@Test
	public void t1() throws IOException {
		assertThat("410C1000", 1024);
	}
}
