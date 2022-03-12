package org.obd.metrics.codec.alfa;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class AcceleratorPedalPostionTest implements Mode22Test {
	
	@Test
	public void case_01() {
		final Map<String, Number> mappings = new HashMap<String, Number>() {
			private static final long serialVersionUID = 1L;
			{
				put("6219240000", 0.0); //lean mixture
			}
		};

		mappings.forEach((k, v) -> {
			assertEquals(k, v);
		});
		
	}
}
