package org.openobd2.core.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.PidTest;

public class IgnitationAngleReductionTest implements PidTest{
	@Test
	public void cylinder1() throws IOException {
		mode22Test("186C", "62186C00", 0.0);
	}
}
