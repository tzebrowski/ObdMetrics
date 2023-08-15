package org.obd.metrics.executor;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.PIDsRegistryFactory;
import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.executor.MetricValidator.MetricValidatorStatus;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;

public class MetricValidatorTest {

	@Test
	void value_okTest() {
		PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json");

		PidDefinition coolant = pidRegistry.findBy(6l);
		Assertions.assertThat(coolant).isNotNull();

		ObdMetric metric = ObdMetric.builder().command(new ObdCommand(coolant)).value(120).build();
		MetricValidator metricValidator = new MetricValidator();
		MetricValidatorStatus status = metricValidator.validate(metric);

		Assertions.assertThat(status).isEqualTo(MetricValidatorStatus.OK);
	}

	@Test
	void bellow_minTest() {
		PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json");

		PidDefinition coolant = pidRegistry.findBy(6l);
		Assertions.assertThat(coolant).isNotNull();

		ObdMetric metric = ObdMetric.builder().command(new ObdCommand(coolant)).value(-150).build();
		MetricValidator metricValidator = new MetricValidator();
		MetricValidatorStatus status = metricValidator.validate(metric);

		Assertions.assertThat(status).isEqualTo(MetricValidatorStatus.BELLOW_MIN);
	}

	@Test
	void above_maxTest() {
		PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json");

		PidDefinition coolant = pidRegistry.findBy(6l);
		Assertions.assertThat(coolant).isNotNull();

		ObdMetric metric = ObdMetric.builder().command(new ObdCommand(coolant)).value(150).build();
		MetricValidator metricValidator = new MetricValidator();
		MetricValidatorStatus status = metricValidator.validate(metric);

		Assertions.assertThat(status).isEqualTo(MetricValidatorStatus.ABOVE_MAX);
	}

	@Test
	void null_valueTest() {
		PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json");

		PidDefinition coolant = pidRegistry.findBy(6l);
		Assertions.assertThat(coolant).isNotNull();

		ObdMetric metric = ObdMetric.builder().command(new ObdCommand(coolant)).value(null).build();
		MetricValidator metricValidator = new MetricValidator();
		MetricValidatorStatus status = metricValidator.validate(metric);

		Assertions.assertThat(status).isEqualTo(MetricValidatorStatus.NULL_VALUE);
	}
	
	@Test
	void in_alertTest() {
		PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json");

		PidDefinition coolant = pidRegistry.findBy(6l);
		coolant.setAlertThreshold(50);
		Assertions.assertThat(coolant).isNotNull();

		ObdMetric metric = ObdMetric.builder().command(new ObdCommand(coolant)).value(60).build();
		MetricValidator metricValidator = new MetricValidator();
		MetricValidatorStatus status = metricValidator.validate(metric);

		Assertions.assertThat(status).isEqualTo(MetricValidatorStatus.IN_ALERT);
	}
}
