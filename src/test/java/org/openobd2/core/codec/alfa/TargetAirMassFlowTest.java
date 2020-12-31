package org.openobd2.core.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.PidTest;

public class TargetAirMassFlowTest implements PidTest {
	@Test
	public void t1() throws IOException {
		mode22Test("1867", "6218670059", 8);
	}
}
