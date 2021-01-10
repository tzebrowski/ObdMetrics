package org.openobd2.core.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.PidTest;

public class IntakePressureTest implements PidTest {
	
	@Test
	public void t2() throws IOException {
		mode22Test("62193732b4", 995.0);
	}
	
	@Test
	public void t1() throws IOException {
		mode22Test("62193731E7", 995.0);
	}
}
