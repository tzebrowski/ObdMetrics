package org.obd.metrics.codec.mode1;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class EngineCalculatedLoadTest implements Mode01Test {
	@Test
	public void t1() throws IOException {
		assertThat("410444", 26.67);
	}
}
