package org.obd.metrics.codec.mode1;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.obd.metrics.codec.PidTest;

public class IntakeManifoldPressureTest implements PidTest {
	@Test
	public void t1() throws IOException {
		mode01Test("410b1e", -0.7);
		mode01Test("410b35", -0.47);
		mode01Test("410b62", -0.02);
	}
}
