package org.obd.metrics.codec.alfa;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class EngineTemp implements Mode22Test {
	@Test
	public void case_01() {
		final Map<String, Number> mappings = new HashMap<String, Number>() {
			private static final long serialVersionUID = 1L;
			{
				put("62100340", -1.0);
				put("621003AB", 80.0);
				put("621003AA", 79.0);
				put("621003C0", 96.0);
			}
		};
		mappings.forEach((k, v) -> {
			assertEquals(k, v);
		});	
	}
}
