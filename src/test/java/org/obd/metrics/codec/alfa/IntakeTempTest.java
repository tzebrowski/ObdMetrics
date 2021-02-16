package org.obd.metrics.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class IntakeTempTest  implements Mode22Test{
	@Test
	public void t0() throws IOException {
		assertThat("62193540",-1);//0
	}
	
	@Test
	public void t1() throws IOException {
		assertThat("62193542",1);//2.0
	}
}
