package org.obd.metrics.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.obd.metrics.codec.PidTest;

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
