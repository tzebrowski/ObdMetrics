package org.openobd2.core.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.PidTest;

public class IntakePressureTest implements PidTest {
	
	@Test
	public void t2() throws IOException {
		//
		mode22Test("62193732b4", 1025);
	}
	
	@Test
	public void t1() throws IOException {
		mode22Test("62193731E7", 1009);
	}
	

	@Test
	public void t3() throws IOException {
		mode22Test("6219373307", 1031);
	}
}
