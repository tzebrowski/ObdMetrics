package org.obd.metrics.codec.mode1;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class O2_SensorVoltageTest implements Mode01Test {
	
	@Test
	public void case_01() {
		final Map<String, Number> mappings = new HashMap<String, Number>() {
			private static final long serialVersionUID = 1L;
			{
				put("41175aff", 0.45); //lean mixture
				put("4117b4ff", 0.9); //reach mixture
			}
		};

		mappings.forEach((k, v) -> {
			assertEquals(k, v);
		});
	}
}
