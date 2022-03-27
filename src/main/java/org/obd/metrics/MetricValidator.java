package org.obd.metrics;

import org.obd.metrics.pid.PidDefinition;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class MetricValidator {

	static enum MetricValidatorStatus {
		ABOVE_MAX, BELLOW_MIN, OK
	}

	MetricValidatorStatus validate(final ObdMetric metric) {
		final PidDefinition pid = metric.command.getPid();
		
		if (metric.value == null) {
			return MetricValidatorStatus.OK;
		}

		if (pid.getMax() == null) {
			return MetricValidatorStatus.OK;
		}
		
		if (pid.getMin() == null) {
			return MetricValidatorStatus.OK;
		}
		
		if (metric.valueToLong() > pid.getMax().longValue()) {
			if (log.isWarnEnabled()) {
				log.warn("Metric {} is above the max value ({}). Current value: {}", pid.getPid(),
				        pid.getMax().longValue(), metric.getValue());
			}

			return MetricValidatorStatus.ABOVE_MAX;
		}

		if (metric.valueToLong() < pid.getMin().longValue()) {
			if (log.isWarnEnabled()) {
				log.warn("Metric {} is bellow the min value({}). Current value: {}", pid.getPid(),
				        pid.getMin().longValue(), metric.getValue());
			}
			return MetricValidatorStatus.BELLOW_MIN;
		}

		return MetricValidatorStatus.OK;
	}

}
