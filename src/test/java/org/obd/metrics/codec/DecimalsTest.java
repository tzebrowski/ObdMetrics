package org.obd.metrics.codec;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

public class DecimalsTest {

	@ParameterizedTest
	@CsvSource(value = { 
		"139;8b",
		"90;5a",
	}, delimiter = ';')
	void decimalTest(String actual, String expected) {
		Assertions.assertThat(Integer.valueOf(actual)).isEqualTo(Integer.parseInt(expected, 16));
		
		ConnectorResponse wrap = ConnectorResponseFactory.wrap(expected.getBytes());
		Assertions.assertThat(Integer.valueOf(actual)).isEqualTo(wrap.toDecimal(0));
	}
}
