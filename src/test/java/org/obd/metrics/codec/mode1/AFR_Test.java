package org.obd.metrics.codec.mode1;

import java.io.IOException;


import org.junit.jupiter.api.Test;
//http://jimsprojectgarage.weebly.com/edelbrock-carb-tuning-with-a-narrowband-oxygen-sensor.html
public class AFR_Test implements Mode01Test {
	
	@Test
	public void lean_mixture() throws IOException {
		assertEquals("41155aff", 15.12);
	}
	
	@Test
	public void reach_mixture() throws IOException {
		
		assertEquals("4115b4ff", 12.61);
	}
}
