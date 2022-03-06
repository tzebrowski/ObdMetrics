package org.obd.metrics.codec.mode1;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.codec.Codec;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.model.RawMessage;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;

public class BatteryVoltageTest implements Mode01Test {
	@Test
	public void case_01() {
		final CodecRegistry codecRegistry = CodecRegistry.builder().equationEngine("JavaScript").build();
		PidDefinitionRegistry pidRegistry = PidRegistryCache.get("extra.json");
		
		final PidDefinition pidDef = pidRegistry.findBy(9000l);
		Assertions.assertThat(pidDef).isNotNull();
		Codec<?> codec = codecRegistry.findCodec(new ObdCommand(pidDef));
		Object value = codec.decode(pidDef, RawMessage.instance("13.4v".getBytes()));
		
		Assertions.assertThat(value).isEqualTo(13.4);
	}
}
