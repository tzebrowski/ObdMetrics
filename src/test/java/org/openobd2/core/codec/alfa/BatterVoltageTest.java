package org.openobd2.core.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.PidTest;

public class BatterVoltageTest implements PidTest {
	@Test
	public void t1() throws IOException {
		mode22Test("1004", "62100496", 14.0);
	}

	@Test
	public void t2() throws IOException {
		mode22Test("1004", "62100482", 12.0);
	}

}
