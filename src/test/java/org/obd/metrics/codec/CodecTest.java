package org.obd.metrics.codec;

import org.assertj.core.api.Assertions;
import org.obd.metrics.PidRegistryCache;
import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.raw.RawMessage;

public interface CodecTest {

	default void assertEquals(String pid, String pidSource, String rawData, Object expectedValue) {
		assertEquals(false, pid, pidSource, rawData, expectedValue);
	}

	default void assertEquals(boolean debug, String pid, String pidSource, String rawData, Object expectedValue) {

		Assertions.assertThat(pid).isNotNull();
		Assertions.assertThat(pidSource).isNotNull();
		Assertions.assertThat(rawData).isNotNull();

		final CodecRegistry codecRegistry = CodecRegistry.builder()
				.formulaEvaluatorConfig(FormulaEvaluatorConfig
						.builder()
						.debug(debug)
						.scriptEngine("JavaScript")
						.build()).build();
		
		final PidDefinition pidDef = PidRegistryCache.get(pidSource).findBy(pid);
		Assertions.assertThat(pidDef).isNotNull();
		final Codec<?> codec = codecRegistry.findCodec(pidDef);

		if (codec == null) {
			Assertions.fail("No codec available for PID: {}", pid);
		} else {
			final Object actualValue = codec.decode(pidDef, RawMessage.wrap(rawData.getBytes()));
			Assertions.assertThat(actualValue).isEqualTo(expectedValue);
		}
	}
}
