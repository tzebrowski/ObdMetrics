package org.openobd2.core;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.command.obd.mode1.EngineTempCommand;
import org.openobd2.core.command.obd.mode1.VehicleSpeedCommand;

public class VehicleSpeedCommandTest {
	@Test
	public void positiveTest() {
		String rawData = "410D3F";
		long temp = new VehicleSpeedCommand().convert(rawData);
		Assertions.assertThat(temp).isEqualTo(63);

		rawData = "410d00";
		temp = new VehicleSpeedCommand().convert(rawData);
		Assertions.assertThat(temp).isEqualTo(0);
	}
}
