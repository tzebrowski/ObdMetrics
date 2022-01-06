package org.obd.metrics.codec.alfa;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class FuelLevelTest implements Mode22Test {
	@Test
	public void case_01() {
		final Map<String, Number> mappings = new HashMap<String, Number>() {
			private static final long serialVersionUID = 1L;
			{
				put("62100122", 17.0);
			}
		};
		mappings.forEach((k, v) -> {
			assertEquals(k, v);
		});	
	}
}
