package org.openobd2.core;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.command.EngineTempCommand;

public class EngineTempCommandTest {
	@Test
	public void positiveTest() {
		String rawData = "410522";
		int temp = new EngineTempCommand().convert(rawData);
		Assertions.assertThat(temp).isEqualTo(-6);

		rawData = "410517";
		temp = new EngineTempCommand().convert(rawData);
		Assertions.assertThat(temp).isEqualTo(-17);
	}
}
