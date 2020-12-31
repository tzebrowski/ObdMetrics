package org.openobd2.core.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.PidTest;

public class AirMassFlowTest implements PidTest {
	@Test
	public void t1() throws IOException {
		mode22Test("180E", "62180E0069", 10.5);
	}

	@Test
	public void t2() throws IOException {
		mode22Test("180E", "62180E015C", 34.8);
	}
}
