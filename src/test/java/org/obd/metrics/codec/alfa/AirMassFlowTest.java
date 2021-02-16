package org.obd.metrics.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class AirMassFlowTest implements Mode22Test {
	@Test
	public void t1() throws IOException {
		assertThat("62180E0069", 10.5);
	}

	@Test
	public void t2() throws IOException {
		assertThat("62180E015C", 34.8);
	}
}
