package org.openobd2.core.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.PidTest;

public class AcceleratorPedalPostionTest implements PidTest {
	@Test
	public void possitiveTest() throws IOException {
		mode22Test("1924", "6219240000", 0.0);
	}
}
