package org.openobd2.core;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.command.EngineRpmCommand;
import org.openobd2.core.command.EngineTempCommand;

public class EngineRpmCommandTest {
	@Test
	public void positiveTest() {
		String rawData = "410c0000";
		int temp = new EngineRpmCommand().convert(rawData);
		Assertions.assertThat(temp).isEqualTo(0);

		rawData = "410c541B";
		temp = new EngineRpmCommand().convert(rawData);
		Assertions.assertThat(temp).isEqualTo(5382);
	}

	@Test
	public void negativeTest() {
		String rawData = "410d0000";
		Integer temp = new EngineRpmCommand().convert(rawData);
		Assertions.assertThat(temp).isNull();
	}
}
