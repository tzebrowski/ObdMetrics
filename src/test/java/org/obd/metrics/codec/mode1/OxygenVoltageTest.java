package org.obd.metrics.codec.mode1;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class OxygenVoltageTest implements Mode01Test {

	@Test
	public void t1() throws IOException {
		assertThat("41175aff", 0.45);
	}
}
