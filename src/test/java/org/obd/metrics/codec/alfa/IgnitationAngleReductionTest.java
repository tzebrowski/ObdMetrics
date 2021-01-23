package org.obd.metrics.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.obd.metrics.codec.PidTest;

public class IgnitationAngleReductionTest implements PidTest {
	@Test
	public void cylinder1() throws IOException {
		mode22Test("62186C00", 0.0);
	}
}
