package org.obd.metrics.codec.dtc;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.PidRegistryCache;
import org.obd.metrics.command.dtc.DtcCommand;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.raw.RawMessage;

public class DtcDecoderTest {

	@Test
	public void erros_available_case_1() {
		// P26E4-00
		// P2BC1-00
		// U1008-00
		final String rx = "00F0:5902CF26E4001:482BC10048D0082:00480";
		final PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("giulia_2.0_gme.json");
		final PidDefinition pid = pidDefinitionRegistry.findBy(27000l);

		final List<String> list = new DtcCommand(pid).decode(null, RawMessage.wrap(rx.getBytes()));
		Assertions.assertThat(list).contains("26E400").contains("D00800").contains("2BC100");
	}

	@Test
	public void error_available_case_2() {
		// C405810
		final String rx = "5902CFC4058108";
		final PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("giulia_2.0_gme.json");
		final PidDefinition pid = pidDefinitionRegistry.findBy(27000l);

		final List<String> list = new DtcCommand(pid).decode(null, RawMessage.wrap(rx.getBytes()));
		Assertions.assertThat(list).contains("C40581");
	}

	@Test
	public void no_errors_available_case_1() {
		// C405810
		final String rx = "5902CF";
		final PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("giulia_2.0_gme.json");
		final PidDefinition pid = pidDefinitionRegistry.findBy(27000l);

		final List<String> list = new DtcCommand(pid).decode(null, RawMessage.wrap(rx.getBytes()));
		Assertions.assertThat(list).isEmpty();
	}
}
