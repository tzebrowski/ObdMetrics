package org.obd.metrics.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class TargetEngineRpmTest implements Mode22Test {

	@Test
	public void targetRpmTest() throws IOException {
		assertThat("62186B6E", 1100.0);
	}
}
