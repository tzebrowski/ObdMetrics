package org.obd.metrics.codec;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class DecimalsTest {

	@Test
	void toDecimalTest() {
		String value = "8b";

		Assertions.assertThat(139).isEqualTo(Integer.parseInt(value, 16));
		Assertions.assertThat(139).isEqualTo(new Decimals().toDecimal(value.getBytes()));
		value = "5a";

		Assertions.assertThat(90).isEqualTo(Integer.parseInt(value, 16));
		Assertions.assertThat(90).isEqualTo(new Decimals().toDecimal(value.getBytes()));
	}
}
