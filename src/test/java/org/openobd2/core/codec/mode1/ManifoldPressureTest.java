package org.openobd2.core.codec.mode1;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.PidTest;

public class ManifoldPressureTest implements PidTest {
	@Test
	public void t1() throws IOException {
		mode01Test("410b1e", -0.7);
		mode01Test("410b35", -0.47);
	}
}
