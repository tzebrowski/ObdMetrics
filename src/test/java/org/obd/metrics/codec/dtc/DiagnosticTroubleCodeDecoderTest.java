package org.obd.metrics.codec.dtc;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.PidRegistryCache;
import org.obd.metrics.api.model.DiagnosticTroubleCode;
import org.obd.metrics.command.dtc.DiagnosticTroubleCodeCommand;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.raw.RawMessage;

public class DiagnosticTroubleCodeDecoderTest {

	@Test
	public void erros_available_case_1() {
		// P26E4-00
		// P2BC1-00
		// U1008-00
		final String rx = "00F0:5902CF26E4001:482BC10048D0082:00480";
		final PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("giulia_2.0_gme.json");
		final PidDefinition pid = pidDefinitionRegistry.findBy(27000l);

		final List<DiagnosticTroubleCode> list = new DiagnosticTroubleCodeCommand(pid).decode(null, RawMessage.wrap(rx.getBytes()));
		Assertions.assertThat(list)
			.contains(new DiagnosticTroubleCode("26E400",null))
			.contains(new DiagnosticTroubleCode("D00800",null))
			.contains(new DiagnosticTroubleCode("2BC100",null));
	}

	@Test
	public void error_available_case_2() {
		// C405810
		final String rx = "5902CFC4058108";
		final PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("giulia_2.0_gme.json");
		final PidDefinition pid = pidDefinitionRegistry.findBy(27000l);

		final List<DiagnosticTroubleCode> list = new DiagnosticTroubleCodeCommand(pid).decode(null, RawMessage.wrap(rx.getBytes()));
		Assertions.assertThat(list)
			.contains(new DiagnosticTroubleCode("C40581",null));
	}

	@Test
	public void error_available_case_3() {
		// C405810
		final String rx = "7F197800B0:5902CF0191111:08C4058108";
		final PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("giulia_2.0_gme.json");
		final PidDefinition pid = pidDefinitionRegistry.findBy(27000l);

		final List<DiagnosticTroubleCode> list = new DiagnosticTroubleCodeCommand(pid).decode(null, RawMessage.wrap(rx.getBytes()));
		Assertions.assertThat(list)
			.contains(new DiagnosticTroubleCode("019111",null))
			.contains(new DiagnosticTroubleCode("08C405",null));
	}

	
	@Test
	public void no_errors_available_case_1() {
		// C405810
		final String rx = "5902CF";
		final PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("giulia_2.0_gme.json");
		final PidDefinition pid = pidDefinitionRegistry.findBy(27000l);

		final List<DiagnosticTroubleCode> list = new DiagnosticTroubleCodeCommand(pid).decode(null, RawMessage.wrap(rx.getBytes()));
		Assertions.assertThat(list).isEmpty();
	}
}
