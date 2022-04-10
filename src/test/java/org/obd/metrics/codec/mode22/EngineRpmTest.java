package org.obd.metrics.codec.mode22;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class EngineRpmTest implements Mode22Test {

	@Test
	public void case_01() {
		final Map<String, Number> mappings = new HashMap<String, Number>() {
			private static final long serialVersionUID = 1L;
			{
				put("6210000000", 0.0);
				put("6210000BBC", 751.0);
				put("6210000BEA", 762.5);
			}
		};
		mappings.forEach((k, v) -> {
			assertEquals(k, v);
		});
	}

}
