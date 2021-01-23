package org.obd.metrics.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.obd.metrics.codec.PidTest;

public class AcceleratorPedalPostionTest implements PidTest {
	@Test
	public void possitiveTest() throws IOException {
		mode22Test("6219240000", 0.0);
	}
}
