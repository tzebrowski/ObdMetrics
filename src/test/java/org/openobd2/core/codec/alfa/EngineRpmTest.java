package org.openobd2.core.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.PidTest;

public class EngineRpmTest implements PidTest {

	@Test
	public void t1() throws IOException {
		mode22Test("1000", "6210000000", 0.0);
	}

	@Test
	public void t2() throws IOException {
		mode22Test("1000", "6210000BEA", 762.5);
	}
}
