package org.obd.metrics.codec.mode1;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class VehicleSpeedTest implements Mode01Test {
	@Test
	public void t1() throws IOException {
		assertThat("410D3F", 63);
	}

	@Test
	public void t2() throws IOException {
		assertThat("410d00", 0);
	}
}
