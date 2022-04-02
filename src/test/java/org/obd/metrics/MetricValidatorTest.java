package org.obd.metrics;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.MetricValidator.MetricValidatorStatus;
import org.obd.metrics.codec.CodecTest.PidRegistryCache;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;

public class MetricValidatorTest {

	@Test
	void value_ok() {
		PidDefinitionRegistry pidRegistry = PidRegistryCache.get("mode01.json");

		final PidDefinition coolant = pidRegistry.findBy(6l);
		Assertions.assertThat(coolant).isNotNull();

		ObdMetric metric = ObdMetric.builder().command(new ObdCommand(coolant)).value(120).build();
		MetricValidator metricValidator = new MetricValidator();
		MetricValidatorStatus status = metricValidator.validate(metric);
		
		Assertions.assertThat(status).isEqualTo(MetricValidatorStatus.OK);
	}
	
	@Test
	void bellow_min() {
		PidDefinitionRegistry pidRegistry = PidRegistryCache.get("mode01.json");

		final PidDefinition coolant = pidRegistry.findBy(6l);
		Assertions.assertThat(coolant).isNotNull();

		ObdMetric metric = ObdMetric.builder().command(new ObdCommand(coolant)).value(-150).build();
		MetricValidator metricValidator = new MetricValidator();
		MetricValidatorStatus status = metricValidator.validate(metric);
		
		Assertions.assertThat(status).isEqualTo(MetricValidatorStatus.BELLOW_MIN);
	}
	
	@Test
	void above_max() {
		PidDefinitionRegistry pidRegistry = PidRegistryCache.get("mode01.json");

		final PidDefinition coolant = pidRegistry.findBy(6l);
		Assertions.assertThat(coolant).isNotNull();

		ObdMetric metric = ObdMetric.builder().command(new ObdCommand(coolant)).value(150).build();
		MetricValidator metricValidator = new MetricValidator();
		MetricValidatorStatus status = metricValidator.validate(metric);
		
		Assertions.assertThat(status).isEqualTo(MetricValidatorStatus.ABOVE_MAX);
	}
	

	@Test
	void null_value() {
		PidDefinitionRegistry pidRegistry = PidRegistryCache.get("mode01.json");

		final PidDefinition coolant = pidRegistry.findBy(6l);
		Assertions.assertThat(coolant).isNotNull();

		ObdMetric metric = ObdMetric.builder().command(new ObdCommand(coolant)).value(null).build();
		MetricValidator metricValidator = new MetricValidator();
		MetricValidatorStatus status = metricValidator.validate(metric);
		
		Assertions.assertThat(status).isEqualTo(MetricValidatorStatus.NULL_VALUE);
	}
}
