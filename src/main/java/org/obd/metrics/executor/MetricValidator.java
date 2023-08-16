package org.obd.metrics.executor;

import org.obd.metrics.pid.PidDefinition;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public final class MetricValidator {

	public static enum MetricValidatorStatus {
		ABOVE_MAX, BELLOW_MIN, OK, NULL_VALUE, IN_ALERT_UPPER, IN_ALERT_LOWER
	}

	public MetricValidatorStatus validate(final PidDefinition pid, final Number value) {
		if (value == null) {
			return MetricValidatorStatus.NULL_VALUE;
		}

		final double doubleValue = value.doubleValue();
		
		if (Double.isNaN(doubleValue)) {
			return MetricValidatorStatus.NULL_VALUE;
		}

		if (doubleValue > pid.getMax().doubleValue()) {
			if (log.isDebugEnabled()) {
				log.debug("Metric {} is above the max value ({}). Current value: {}", pid.getDescription(),
						pid.getMax().longValue(), value);
			}

			return MetricValidatorStatus.ABOVE_MAX;
		}

		if (doubleValue < pid.getMin().doubleValue()) {
			if (log.isDebugEnabled()) {
				log.debug("Metric {} is bellow the min value({}). Current value: {}", pid.getDescription(),
						pid.getMin().longValue(), value);
			}
			return MetricValidatorStatus.BELLOW_MIN;
		}

		if (pid.getAlertUpperThreshold() != null && 
				doubleValue > pid.getAlertUpperThreshold().doubleValue()) {
			return MetricValidatorStatus.IN_ALERT_UPPER;
		}

		if (pid.getAlertLowerThreshold() != null && 
				doubleValue < pid.getAlertLowerThreshold().doubleValue()) {
			return MetricValidatorStatus.IN_ALERT_LOWER;
		}

		return MetricValidatorStatus.OK;
	}
}
