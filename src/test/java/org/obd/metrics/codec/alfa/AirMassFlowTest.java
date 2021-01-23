package org.obd.metrics.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.obd.metrics.codec.PidTest;

public class AirMassFlowTest implements PidTest {
	@Test
	public void t1() throws IOException {
		mode22Test("62180E0069", 10.5);
	}

	@Test
	public void t2() throws IOException {
		mode22Test("62180E015C", 34.8);
	}
}
