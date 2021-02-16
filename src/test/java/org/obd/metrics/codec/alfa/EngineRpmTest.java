package org.obd.metrics.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class EngineRpmTest implements Mode22Test {

	@Test
	public void t1() throws IOException {
		assertThat("6210000000", 0.0);
	}

	@Test
	public void t2() throws IOException {
		assertThat("6210000BEA", 762.5);
	}
}
