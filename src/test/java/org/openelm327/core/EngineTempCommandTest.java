package org.openelm327.core;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openelm327.core.command.EngineTempCommand;

public class EngineTempCommandTest {
	@Test
	public void positiveTest() {
		String rawData = "41 05 22";
		int temp = new EngineTempCommand().transform(rawData);
		Assertions.assertThat(temp).isEqualTo(-6);

		rawData = "41 05 17";
		temp = new EngineTempCommand().transform(rawData);
		Assertions.assertThat(temp).isEqualTo(-17);
	}
}
