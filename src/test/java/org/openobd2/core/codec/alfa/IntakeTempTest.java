package org.openobd2.core.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.PidTest;

public class IntakeTempTest  implements PidTest{
	@Test
	public void t0() throws IOException {
		mode22Test("62193540",-1);//0
	}
	
	@Test
	public void t1() throws IOException {
		mode22Test("62193542",1);//2.0
	}
	
}
