package org.obd.metrics.codec.mode22;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class TargetIntakePressureTest implements Mode22Test {

	@Test
	public void case_01() throws IOException {
		final Map<String, Number> mappings = new HashMap<String, Number>() {
			private static final long serialVersionUID = 1L;
			{
				put("62181F63CE", 990.0);
				put("62181F2424", 359.0);
			}
		};
		mappings.forEach((k, v) -> {
			assertEquals(k, v);
		});
	}
}
