package org.obd.metrics.executor;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
	
	@ParameterizedTest
	@CsvSource(
			value = { 
				"7014=4.7",
				"7018=0.9",
				"7020=59"
			},
			delimiter = '=')
	void in_LowerAlertTest(String pidId, String value) {

		PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("giulia_2.0_gme.json");
		PidDefinition pid = pidRegistry.findBy(Long.parseLong(pidId));
		Assertions.assertThat(pid).isNotNull();

		MetricValidatorStatus status = new MetricValidator().validate(pid, Double.parseDouble(value));

		Assertions.assertThat(status).isEqualTo(MetricValidatorStatus.IN_ALERT_LOWER);
	}
	
	
	@ParameterizedTest
	@CsvSource(
			value = { 
				"7015=81",
				"7001=3001",
				"7002=61",
				"7016=901",
				"7017=61",
				"7025=91",
				"7003=111",
				"7004=111",
				"7009=111",
				"7005=3001",
				"7006=3001"
			},
			delimiter = '=')
	void in_UpperAlertTest(String pidId, String value) {

		PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("giulia_2.0_gme.json");
		PidDefinition pid = pidRegistry.findBy(Long.parseLong(pidId));
		Assertions.assertThat(pid).isNotNull();

		MetricValidatorStatus status = new MetricValidator().validate(pid, Double.parseDouble(value));

		Assertions.assertThat(status).isEqualTo(MetricValidatorStatus.IN_ALERT_UPPER);
	}
}
