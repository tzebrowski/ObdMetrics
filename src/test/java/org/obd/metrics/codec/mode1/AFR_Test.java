package org.obd.metrics.codec.mode1;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
//http://jimsprojectgarage.weebly.com/edelbrock-carb-tuning-with-a-narrowband-oxygen-sensor.html
public class AFR_Test implements Mode01Test {
	
	@Test
	public void case_01() throws IOException {
		
		final Map<String, Number> mappings = new HashMap<String, Number>() {
			private static final long serialVersionUID = 1L;
			{
				put("41155aff", 15.12); //lean mixture
				put("4115b4ff", 12.61); //reach mixture
			}
		};

		
		final String a = String.format("%02X", 10);
		final String b = String.format("%02X", 20);
		mappings.put("4115" + a + b, 17.36);
		
		
		mappings.forEach((k, v) -> {
			assertEquals(k, v);
		});
	}
}
