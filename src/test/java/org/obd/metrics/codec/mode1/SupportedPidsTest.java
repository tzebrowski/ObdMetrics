package org.obd.metrics.codec.mode1;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.command.obd.SupportedPidsCommand;
import org.obd.metrics.raw.RawMessage;

public class SupportedPidsTest {

	@Test
	public void invalidDataTest() {

		final String rawData = "2100BE3E2F00";
		final SupportedPidsCommand codec = new SupportedPidsCommand("00");
		final List<String> result = codec.decode(codec.getPid(),RawMessage.instance(rawData));
		Assertions.assertThat(result).isNotNull().isEmpty();
	}

	@Test
	public void pids00() {

		final String rawData = "4100BE3E2F00";
		final SupportedPidsCommand codec = new SupportedPidsCommand("00");
		final List<String> result = codec.decode(codec.getPid(),RawMessage.instance(rawData));

		Assertions.assertThat(result).isNotNull().isNotEmpty().containsExactly("01", "03", "04", "05", "06",
		        "07", "0b", "0c", "0d", "0e", "0f", "13", "15", "16", "17", "18");

	}

	@Test
	public void pids20() {

		final String rawData = "4120a0001000";
		final SupportedPidsCommand codec = new SupportedPidsCommand("20");
		final List<String> result = codec.decode(codec.getPid(),RawMessage.instance(rawData));
		Assertions.assertThat(result).isNotNull().isNotEmpty().containsExactly("01", "03", "14");
	}
}
