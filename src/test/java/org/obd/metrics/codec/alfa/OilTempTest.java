package org.obd.metrics.codec.alfa;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class OilTempTest implements Mode22Test {
	@Test
	public void case_01() {
		
		final Map<String, Number> mappings = new HashMap<String, Number>() {
			private static final long serialVersionUID = 1L;
			{
				put("62194f2d85", 0.0);
				put("62194F3BE5", 113);
				put("62194F2D85", -0.027);
				put("62194F3B85", 83.97);
				put("62194F3E65", 101);
				put("62194f2da5", 2.0);
			}
		};
		mappings.forEach((k, v) -> {
			assertEquals(k, v);
		});
	}


}
