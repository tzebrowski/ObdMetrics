package org.obd.metrics.codec.mode1;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class EngineTempTest implements Mode01Test {

	@Test
	public void t1() throws IOException {
		assertThat("410522", -6);
	}

	@Test
	public void t2() throws IOException {
		assertThat("410517", -17);
	}
}
