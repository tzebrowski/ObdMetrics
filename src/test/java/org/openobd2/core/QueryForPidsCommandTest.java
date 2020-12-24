package org.openobd2.core;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openobd2.core.command.SupportedPidsCommand;

public class QueryForPidsCommandTest {

	@Test
	public void positiveTest() {
		String pids = "4100BE3E2F00";
		final List<String> supportedPids = new SupportedPidsCommand("00").convert(pids);

		Assertions.assertThat(supportedPids).isNotNull().isNotEmpty().containsExactly("01", "03", "04", "05", "06",
				"07", "0b", "0c", "0d", "0e", "0f", "13", "15", "16", "17", "18");
	}

}
