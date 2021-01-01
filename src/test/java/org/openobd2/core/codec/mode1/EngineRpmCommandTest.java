package org.openobd2.core.codec.mode1;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.PidTest;

public class EngineRpmCommandTest implements PidTest {
	@Test
	public void possitiveTest() throws IOException {
		mode01Test("410c541B", 5382.75);
	}
}
