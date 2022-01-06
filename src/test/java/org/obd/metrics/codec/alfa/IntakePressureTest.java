package org.obd.metrics.codec.alfa;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class IntakePressureTest implements Mode22Test {

	@Test
	public void case_01() {
		final Map<String, Number> mappings = new HashMap<String, Number>() {
			private static final long serialVersionUID = 1L;
			{
				put("62193732b4", 1025.0);
				put("62193731E7", 1009.0);
				put("6219373307", 1031.0);
			}
		};
		mappings.forEach((k, v) -> {
			assertEquals(k, v);
		});	
	}
}
