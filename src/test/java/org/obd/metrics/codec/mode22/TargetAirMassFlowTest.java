package org.obd.metrics.codec.mode22;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class TargetAirMassFlowTest implements Mode22Test {
	@Test
	public void case_01() {
		final Map<String, Number> mappings = new HashMap<String, Number>() {
			private static final long serialVersionUID = 1L;
			{
				put("6218670059", 8.9);
			}
		};
		mappings.forEach((k, v) -> {
			assertEquals(k, v);
		});
	}
}
