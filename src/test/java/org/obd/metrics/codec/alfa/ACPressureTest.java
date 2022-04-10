package org.obd.metrics.codec.alfa;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class ACPressureTest implements Mode22Test {
	@Test
	public void case_01() {
		final Map<String, Number> mappings = new HashMap<String, Number>() {
			private static final long serialVersionUID = 1L;
			{
				put("62192F24",19.0); //lean mixture
			}
		};

		mappings.forEach((k, v) -> {
			assertEquals(k, v);
		});
	}
}
