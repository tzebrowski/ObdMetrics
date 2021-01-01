package org.openobd2.core.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.PidTest;

public class FuelLevelTest implements PidTest {
	@Test
	public void t1() throws IOException {
		mode22Test("62100122", 17.0);
	}
}
