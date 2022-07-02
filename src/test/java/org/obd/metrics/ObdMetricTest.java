package org.obd.metrics;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.codec.CodecTest.PidRegistryCache;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;

public class ObdMetricTest {

	@Test
	void conversion() {
		PidDefinitionRegistry pidRegistry = PidRegistryCache.get("mode01.json");

		final PidDefinition coolant = pidRegistry.findBy(6l);
		Assertions.assertThat(coolant).isNotNull();

		ObdMetric metric = ObdMetric.builder().command(new ObdCommand(coolant)).value(-150).build();
		Assertions.assertThat(metric.valueToLong()).isEqualTo(-150l);
		Assertions.assertThat(metric.valueToDouble()).isEqualTo(-150.0);
		Assertions.assertThat(metric.valueToString()).isEqualTo("-150");
		Assertions.assertThat(metric.getValue()).isEqualTo(-150);
	}
	
	
	@Test
	void null_value() {
		PidDefinitionRegistry pidRegistry = PidRegistryCache.get("mode01.json");

		final PidDefinition coolant = pidRegistry.findBy(6l);
		Assertions.assertThat(coolant).isNotNull();

		ObdMetric metric = ObdMetric.builder().command(new ObdCommand(coolant)).value(null).build();
		Assertions.assertThat(metric.valueToLong()).isEqualTo(-40l);
		Assertions.assertThat(metric.valueToDouble()).isEqualTo(-40.0);
		Assertions.assertThat(metric.valueToString()).isEqualTo("No data");
		Assertions.assertThat(metric.getValue()).isEqualTo(null);
	}
	
	
	@Test
	void double_value() {
		PidDefinitionRegistry pidRegistry = PidRegistryCache.get("mode01.json");

		final PidDefinition coolant = pidRegistry.findBy(6l);
		Assertions.assertThat(coolant).isNotNull();

		ObdMetric metric = ObdMetric.builder().command(new ObdCommand(coolant)).value(20.12345d).build();
		Assertions.assertThat(metric.valueToLong()).isEqualTo(20);
		Assertions.assertThat(metric.valueToDouble()).isEqualTo(20.12d);
		Assertions.assertThat(metric.valueToString()).isEqualTo("20.12");
		Assertions.assertThat(metric.getValue()).isEqualTo(20.12345d);
	}
}
