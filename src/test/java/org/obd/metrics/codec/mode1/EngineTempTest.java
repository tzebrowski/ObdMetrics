package org.obd.metrics.codec.mode1;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.obd.metrics.codec.PidTest;

public class EngineTempTest implements PidTest {

	@Test
	public void t1() throws IOException {
		mode01Test("410522", -6);
	}

	@Test
	public void t2() throws IOException {
		mode01Test("410517", -17);
	}
}
