package org.obd.metrics.codec.mode22;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class AirMassFlowTest implements Mode22Test {
	@Test
	public void case_01() {
		final Map<String, Number> mappings = new HashMap<String, Number>() {
			private static final long serialVersionUID = 1L;
			{
				put("62180E0069",10.5); 
				put("62180E015C", 34.8);
			}
		};
		mappings.forEach((k, v) -> {
			assertEquals(k, v);
		});
	}
}
