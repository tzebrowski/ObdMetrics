package org.obd.metrics.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class FuelLevelTest implements Mode22Test {
	@Test
	public void t1() throws IOException {
		assertThat("62100122", 17.0);
	}
}
