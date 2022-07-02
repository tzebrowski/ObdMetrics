package org.obd.metrics.executor;

import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.pid.PidDefinition;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor()
final class MetricValidator {

	static enum MetricValidatorStatus {
		ABOVE_MAX, BELLOW_MIN, OK, NULL_VALUE
	}

	MetricValidatorStatus validate(final ObdMetric metric) {
		final PidDefinition pid = metric.getCommand().getPid();

		if (metric.getValue() == null) {
			return MetricValidatorStatus.NULL_VALUE;
		}

		if (metric.valueToLong() > pid.getMax().longValue()) {
			if (log.isWarnEnabled()) {
				log.warn("Metric {} is above the max value ({}). Current value: {}", pid.getDescription(),
				        pid.getMax().longValue(), metric.getValue());
			}

			return MetricValidatorStatus.ABOVE_MAX;
		}

		if (metric.valueToLong() < pid.getMin().longValue()) {
			if (log.isWarnEnabled()) {
				log.warn("Metric {} is bellow the min value({}). Current value: {}", pid.getDescription(),
				        pid.getMin().longValue(), metric.getValue());
			}
			return MetricValidatorStatus.BELLOW_MIN;
		}

		return MetricValidatorStatus.OK;
	}
}
