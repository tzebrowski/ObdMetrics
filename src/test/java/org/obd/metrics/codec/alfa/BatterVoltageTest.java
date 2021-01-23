package org.obd.metrics.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.obd.metrics.codec.PidTest;

public class BatterVoltageTest implements PidTest {
	@Test
	public void t1() throws IOException {
		mode22Test("62100496", 14.0);
	}

	@Test
	public void t2() throws IOException {
		mode22Test("62100482", 12.0);
	}

}
