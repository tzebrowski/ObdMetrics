package org.openobd2.core.codec.mode1;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.PidTest;

public class VehicleSpeedTest implements PidTest {
	@Test
	public void t1() throws IOException {
		mode01Test("410D3F", 63);
	}

	@Test
	public void t2() throws IOException {
		mode01Test("410d00", 0);
	}
}
