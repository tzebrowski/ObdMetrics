package org.openobd2.core.codec.mode1;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.PidTest;

public class EngineCalculatedLoadTest implements PidTest {
	@Test
	public void t1() throws IOException {
		mode01Test("410444", 26.67);
	}
}
