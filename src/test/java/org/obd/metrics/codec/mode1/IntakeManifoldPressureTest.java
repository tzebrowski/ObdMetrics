package org.obd.metrics.codec.mode1;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class IntakeManifoldPressureTest implements Mode01Test {
	@Test
	public void t1() throws IOException {
		assertThat("410b1e", -0.7);
		assertThat("410b35", -0.47);
		assertThat("410b62", -0.02);
	}
}
