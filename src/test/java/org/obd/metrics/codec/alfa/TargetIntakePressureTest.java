package org.obd.metrics.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class TargetIntakePressureTest implements Mode22Test {

	@Test
	public void t1() throws IOException {
		assertThat("62181F63CE", 990.0);
	}

	@Test
	public void t2() throws IOException {
		assertThat("62181F2424", 359.0);
	}
}
