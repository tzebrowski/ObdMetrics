package org.openobd2.core.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.PidTest;

public class EngineTemp implements PidTest {
	@Test
	public void t1() throws IOException {
		mode22Test("62100340", 0.0);
	}

	@Test
	public void t2() throws IOException {
		mode22Test("621003AB", 80.25);
	}

	@Test
	public void t3() throws IOException {
		mode22Test("621003AA", 79.50);
	}

	@Test
	public void t4() throws IOException {
		mode22Test("621003C0", 96);
	}
}
