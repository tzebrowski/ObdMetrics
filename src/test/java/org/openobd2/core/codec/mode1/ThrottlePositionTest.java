package org.openobd2.core.codec.mode1;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.PidTest;

public class ThrottlePositionTest implements PidTest {
	@Test
	public void t1() throws IOException {
		mode01Test("41114f", 31);
	}
}
