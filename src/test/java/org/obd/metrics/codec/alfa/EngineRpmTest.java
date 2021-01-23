package org.obd.metrics.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.obd.metrics.codec.PidTest;

public class EngineRpmTest implements PidTest {

	@Test
	public void t1() throws IOException {
		mode22Test("6210000000", 0.0);
	}

	@Test
	public void t2() throws IOException {
		mode22Test("6210000BEA", 762.5);
	}
}
