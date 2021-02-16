package org.obd.metrics.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class IgnitationAngleReductionTest implements Mode22Test {
	@Test
	public void cylinder1() throws IOException {
		assertThat("62186C00", 0.0);
	}
}
