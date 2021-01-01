package org.openobd2.core.codec.mode1;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.PidTest;

public class OxygenVoltageTest implements PidTest {

	@Test
	public void possitiveTest() throws IOException {
		mode01Test("41155aff", 44.6484375);
	}
}
