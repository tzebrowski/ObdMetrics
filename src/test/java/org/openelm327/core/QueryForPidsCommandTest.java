package org.openelm327.core;

import java.io.IOException;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openelm327.core.command.QueryForPidsCommand;

public class QueryForPidsCommandTest {

	@Test
	public void positiveTest() throws IOException, InterruptedException {
		String pids = "41 00 BE 3E 2F 00";
		final List<String> supportedPids = new QueryForPidsCommand("00").transform(pids);
	
		Assertions.assertThat(supportedPids).isNotNull().isNotEmpty()
		.containsExactly("01", "03", "04", "05", "06", "07",
				"0b","0c","0d","0e","0f","13","15","16","17","18");
	}

}
