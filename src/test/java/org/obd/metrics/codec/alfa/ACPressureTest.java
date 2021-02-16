package org.obd.metrics.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class ACPressureTest implements Mode22Test {
	@Test
	public void cylinder1() throws IOException {
		assertThat("62192F24", 19.0);
	}
}
