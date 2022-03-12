package org.obd.metrics.codec.mode1;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
/*
 
 00E0:410BFF0C00001:11000D000F00052:00AAAAAAAAAAAA
0B 7
7 11
00E0:410BFF0C00001:11000D000F00052:00AAAAAAAAAAAA
0C 11
11 17
00E0:410BFF0C00001:11000D000F00052:00AAAAAAAAAAAA
11 19
19 23
00E0:410BFF0C00001:11000D000F00052:00AAAAAAAAAAAA
0D 23
23 27
00E0:410BFF0C00001:11000D000F00052:00AAAAAAAAAAAA
0F 27
27 31
00E0:410BFF0C00001:11000D000F00052:00AAAAAAAAAAAA
05 31
31 35
 
 
 */
public class CoolantTemperatureTest implements Mode01Test {
	@Test
	public void case_01() {
		
		final Map<String, Number> mappings = new HashMap<String, Number>() {
			private static final long serialVersionUID = 1L;
			{
				put("410500", -40);
			}
		};
		
		mappings.forEach((k, v) -> {
			assertEquals(k, v);
		});
		
		String a = "00E0:410BFF0C00001:11000D000F00052:00AAAAAAAAAAAA";
		System.out.println(a.substring(6,8));
	}
}
