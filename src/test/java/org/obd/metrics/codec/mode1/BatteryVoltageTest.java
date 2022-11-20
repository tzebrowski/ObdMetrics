package org.obd.metrics.codec.mode1;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.PIDsRegistryFactory;
import org.obd.metrics.codec.Codec;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

public class BatteryVoltageTest implements Mode01Test {
	@Test
	public void case_01() {
		final CodecRegistry codecRegistry = CodecRegistry
				.builder()
				.formulaEvaluatorConfig(FormulaEvaluatorConfig
						.builder()
						.scriptEngine("JavaScript").build()).build();

		PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("extra.json");

		final PidDefinition pidDef = pidRegistry.findBy(9000l);
		Assertions.assertThat(pidDef).isNotNull();
		Codec<?> codec = codecRegistry.findCodec(pidDef);
		Object value = codec.decode(pidDef, ConnectorResponseFactory.wrap("13.4v".getBytes()));

		Assertions.assertThat(value).isEqualTo(13.4);
	}
}
