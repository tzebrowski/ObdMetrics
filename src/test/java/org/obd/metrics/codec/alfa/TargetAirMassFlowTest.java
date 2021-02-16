package org.obd.metrics.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class TargetAirMassFlowTest implements Mode22Test {
	@Test
	public void t1() throws IOException {
		assertThat("6218670059", 8.9);
	}
}
