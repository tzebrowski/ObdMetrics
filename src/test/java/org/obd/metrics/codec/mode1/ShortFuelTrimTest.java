package org.obd.metrics.codec.mode1;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ShortFuelTrimTest implements Mode01Test {

	@Disabled
	@Test
	public void case_01() {
		assertEquals("41155aff", 44.65);
	}
}
