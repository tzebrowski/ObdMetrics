package org.obd.metrics.codec;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class DecimalsTest {

	@Test
	void toDecimalTest() {
		String value = "8b";

		Assertions.assertThat(139).isEqualTo(Integer.parseInt(value, 16));
		Assertions.assertThat(139).isEqualTo(Decimals.twoBytesToDecimal(value.getBytes(), 0));
		value = "5a";

		Assertions.assertThat(90).isEqualTo(Integer.parseInt(value, 16));
		Assertions.assertThat(90).isEqualTo(Decimals.twoBytesToDecimal(value.getBytes(), 0));
	}
}
