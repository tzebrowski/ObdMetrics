package org.obd.metrics.codec.alfa;

import org.junit.jupiter.api.Test;

public class IntakeTempTest  implements Mode22Test{
	@Test
	public void case1() {
		assertEquals("62193540",-1);//0
	}
	
	@Test
	public void case2() {
		assertEquals("62193542",1);//2.0
	}
}
