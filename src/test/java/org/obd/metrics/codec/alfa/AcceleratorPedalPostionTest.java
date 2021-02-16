package org.obd.metrics.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class AcceleratorPedalPostionTest implements Mode22Test {
	@Test
	public void possitiveTest() throws IOException {
		assertThat("6219240000", 0.0);
	}
}
