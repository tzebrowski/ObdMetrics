package org.obd.metrics.codec.mode1;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class IntakeTempTest implements Mode01Test {
	@Test
	public void case_01() {
		final Map<String, Number> mappings = new HashMap<String, Number>() {
			private static final long serialVersionUID = 1L;
			{
				put("410F2B", 3);
				put("410F2F", 7);
			}
		};

		mappings.forEach((k, v) -> {
			assertEquals(k, v);
		});
	}
}
