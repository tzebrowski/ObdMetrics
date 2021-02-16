package org.obd.metrics.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class TargetIntakePressureTest implements Mode22Test {

	@Test
	public void case1() throws IOException {
		assertEquals("62181F63CE", 990.0);
	}

	@Test
	public void case2() throws IOException {
		assertEquals("62181F2424", 359.0);
	}
}
