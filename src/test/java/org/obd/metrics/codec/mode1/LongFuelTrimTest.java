package org.obd.metrics.codec.mode1;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.obd.metrics.codec.PidTest;

public class LongFuelTrimTest implements PidTest {
	@Test
	public void t1() throws IOException {
		mode01Test("410781", 0.78);
	}
}
