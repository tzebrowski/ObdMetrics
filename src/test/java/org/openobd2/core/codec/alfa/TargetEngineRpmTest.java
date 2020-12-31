package org.openobd2.core.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.PidTest;

public class TargetEngineRpmTest implements PidTest {

	@Test
	public void targetRpmTest() throws IOException {
		mode22Test("186B", "62186B6E", 1100.0);
	}
}
