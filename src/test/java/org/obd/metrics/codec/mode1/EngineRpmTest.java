package org.obd.metrics.codec.mode1;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class EngineRpmTest implements Mode01Test {
	@Test
	public void case_01() {
		final Map<String, Number> mappings = new HashMap<String, Number>() {
			private static final long serialVersionUID = 1L;
			{
				put("410c541B", 5382);
				put("410C1000", 1024);
			}
		};

		mappings.forEach((k, v) -> {
			assertEquals(k, v);
		});
	}
}
