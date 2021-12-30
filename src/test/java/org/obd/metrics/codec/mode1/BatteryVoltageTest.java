package org.obd.metrics.codec.mode1;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.codec.Codec;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidRegistry;

public class BatteryVoltageTest implements Mode01Test {
	@Test
	public void case_01() {
		final CodecRegistry codecRegistry = CodecRegistry.builder().equationEngine("JavaScript").build();
		PidRegistry pidRegistry = PidRegistryCache.get("extra.json");
		
		final PidDefinition pidDef = pidRegistry.findBy(9000l);
		Assertions.assertThat(pidDef).isNotNull();
		final Optional<Codec<?>> codec = codecRegistry.findCodec(new ObdCommand(pidDef));
		Object value = codec.get().decode(pidDef, "13.4v");
		
		Assertions.assertThat(value).isEqualTo(13.4);
	}
}
