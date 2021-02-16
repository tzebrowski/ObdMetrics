package org.obd.metrics.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class EngineTemp implements Mode22Test {
	@Test
	public void t1() throws IOException {
		assertThat("62100340", -1);
	}

	@Test
	public void t2() throws IOException {
		assertThat("621003AB", 80);
	}

	@Test
	public void t3() throws IOException {
		assertThat("621003AA", 79);
	}

	@Test
	public void t4() throws IOException {
		assertThat("621003C0", 96);
	}
}
