package org.obd.metrics.codec.mode22;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class BatterVoltageTest implements Mode22Test {
	@Test
	public void case_01() {
		final Map<String, Number> mappings = new HashMap<String, Number>() {
			private static final long serialVersionUID = 1L;
			{
				put("62100496",14.0); 
				put("62100482", 12.0);
			}
		};
		mappings.forEach((k, v) -> {
			assertEquals(k, v);
		});
	}
}
