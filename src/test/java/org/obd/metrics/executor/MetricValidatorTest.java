package org.obd.metrics.executor;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.PIDsRegistryFactory;
import org.obd.metrics.executor.MetricValidator.MetricValidatorStatus;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;

public class MetricValidatorTest {

	@Test
	void value_okTest() {
		PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json");

		PidDefinition coolant = pidRegistry.findBy(6l);
		Assertions.assertThat(coolant).isNotNull();

		MetricValidator metricValidator = new MetricValidator();
		MetricValidatorStatus status = metricValidator.validate(coolant, 40);

		Assertions.assertThat(status).isEqualTo(MetricValidatorStatus.OK);
	}

	@Test
	void bellow_minTest() {
		PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json");

		PidDefinition coolant = pidRegistry.findBy(6l);
		Assertions.assertThat(coolant).isNotNull();

		MetricValidator metricValidator = new MetricValidator();
		MetricValidatorStatus status = metricValidator.validate(coolant, -150);

		Assertions.assertThat(status).isEqualTo(MetricValidatorStatus.BELLOW_MIN);
	}

	@Test
	void above_maxTest() {
		PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json");

		PidDefinition coolant = pidRegistry.findBy(6l);
		Assertions.assertThat(coolant).isNotNull();

		MetricValidator metricValidator = new MetricValidator();
		MetricValidatorStatus status = metricValidator.validate(coolant, 150);

		Assertions.assertThat(status).isEqualTo(MetricValidatorStatus.ABOVE_MAX);
	}

	@Test
	void null_valueTest() {
		PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json");

		PidDefinition coolant = pidRegistry.findBy(6l);
		Assertions.assertThat(coolant).isNotNull();

		MetricValidator metricValidator = new MetricValidator();
		MetricValidatorStatus status = metricValidator.validate(coolant, null);

		Assertions.assertThat(status).isEqualTo(MetricValidatorStatus.NULL_VALUE);
	}
	
	@Test
	void in_alertTest() {
		PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json");

		PidDefinition coolant = pidRegistry.findBy(6l);
		coolant.setAlertUpperThreshold(50);
		Assertions.assertThat(coolant).isNotNull();

		MetricValidator metricValidator = new MetricValidator();
		MetricValidatorStatus status = metricValidator.validate(coolant, 60);

		Assertions.assertThat(status).isEqualTo(MetricValidatorStatus.IN_ALERT);
	}
}
