package org.openobd2.core.codec.mode1;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.PidTest;

public class EngineRpmTest implements PidTest {
	@Test
	public void t0() throws IOException {
		mode01Test("410c541B", 5382);
	}
	
	
	@Test
	public void t1() throws IOException {
		mode01Test("410C1000", 1024);
	}
}
