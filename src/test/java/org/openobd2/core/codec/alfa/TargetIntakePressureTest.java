package org.openobd2.core.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.PidTest;

public class TargetIntakePressureTest implements PidTest {

	@Test
	public void t1() throws IOException {
		mode22Test("181F", "62181F63CE", 990.0);
	}

	@Test
	public void t2() throws IOException {
		mode22Test("181F", "62181F2424", 359.0);
	}
}
