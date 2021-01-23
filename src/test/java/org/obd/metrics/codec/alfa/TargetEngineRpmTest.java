package org.obd.metrics.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.obd.metrics.codec.PidTest;

public class TargetEngineRpmTest implements PidTest {

	@Test
	public void targetRpmTest() throws IOException {
		mode22Test("62186B6E", 1100.0);
	}
}
