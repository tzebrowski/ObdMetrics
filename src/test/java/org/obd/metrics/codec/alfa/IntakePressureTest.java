package org.obd.metrics.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class IntakePressureTest implements Mode22Test {

	@Test
	public void t2() throws IOException {
		assertThat("62193732b4", 1025);
	}

	@Test
	public void t1() throws IOException {
		assertThat("62193731E7", 1009);
	}

	@Test
	public void t3() throws IOException {
		assertThat("6219373307", 1031);
	}
}
