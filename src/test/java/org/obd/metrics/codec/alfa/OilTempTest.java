package org.obd.metrics.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;

//"formula": "(A*0x100 + B) < 0x8000 ? (A*0x100 + B) : (A*0x100 + B - 0x10000)"

public class OilTempTest implements Mode22Test {
	@Test
	public void t0() throws IOException {
		assertThat("62194f2d85", 0.0);
	}
	
	@Test
	public void t1() throws IOException {
		assertThat("62194F3BE5", 113);
	}

	@Test
	public void t2() throws IOException {
		assertThat("62194F2D85", -0.027);
	}

	@Test
	public void t3() throws IOException {
		assertThat("62194F3B85", 83.97);
	}

	@Test
	public void t4() throws IOException {
		assertThat("62194F3E65", 101);
	}
	
	
	@Test
	public void t5() throws IOException {
		assertThat("62194f2da5", 2.0);
	}
}
