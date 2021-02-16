package org.obd.metrics.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class BatterVoltageTest implements Mode22Test {
	@Test
	public void t1() throws IOException {
		assertThat("62100496", 14.0);
	}

	@Test
	public void t2() throws IOException {
		assertThat("62100482", 12.0);
	}

}
