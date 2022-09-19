package org.obd.metrics.codec.giulietta_qv_med17_3_1;

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
		final PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("alfa.json");
		final PidDefinition pid = pidDefinitionRegistry.findBy(26000l);

		final List<DiagnosticTroubleCode> list = new DiagnosticTroubleCodeCommand(pid).decode(null, RawMessage.wrap(rx.getBytes()));
		Assertions.assertThat(list)
			.contains(DiagnosticTroubleCode.builder().code("26E400").build())
			.contains(DiagnosticTroubleCode.builder().code("D00800").build())
			.contains(DiagnosticTroubleCode.builder().code("2BC100").build());
	}

	@Test
	public void error_available_case_2() {
		// C405810
		final String rx = "5902CFC4058108";
		final PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("alfa.json");
		final PidDefinition pid = pidDefinitionRegistry.findBy(26000l);

		final List<DiagnosticTroubleCode> list = new DiagnosticTroubleCodeCommand(pid).decode(null, RawMessage.wrap(rx.getBytes()));
		Assertions.assertThat(list)
			.contains(DiagnosticTroubleCode.builder().code("C40581").build());
	}

	@Test
	public void error_available_case_3() {
		// C405810
		final String rx = "7F197800B0:5902CF0191111:08C4058108";
		final PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("alfa.json");
		final PidDefinition pid = pidDefinitionRegistry.findBy(26000l);

		final List<DiagnosticTroubleCode> list = new DiagnosticTroubleCodeCommand(pid).decode(null, RawMessage.wrap(rx.getBytes()));
		Assertions.assertThat(list)
			.contains(DiagnosticTroubleCode.builder().code("019111").build())
			.contains(DiagnosticTroubleCode.builder().code("08C405").build());
	}

	
	@Test
	public void no_errors_available_case_1() {
		// C405810
		final String rx = "5902CF";
		final PidDefinitionRegistry pidDefinitionRegistry = PidRegistryCache.get("alfa.json");
		final PidDefinition pid = pidDefinitionRegistry.findBy(26000l);

		final List<DiagnosticTroubleCode> list = new DiagnosticTroubleCodeCommand(pid).decode(null, RawMessage.wrap(rx.getBytes()));
		Assertions.assertThat(list).isEmpty();
	}
}
