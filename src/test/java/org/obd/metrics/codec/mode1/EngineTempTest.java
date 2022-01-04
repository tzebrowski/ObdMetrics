package org.obd.metrics.codec.mode1;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class EngineTempTest implements Mode01Test {

	@Test
	public void case_01() {
		
		final Map<String, Number> mappings = new HashMap<String, Number>() {
			private static final long serialVersionUID = 1L;

			{
				put("410522", -6);
				put("410517",  -17);

			}
		};

		mappings.forEach((k, v) -> {
			assertEquals(k, v);
		});
		
	}

}
