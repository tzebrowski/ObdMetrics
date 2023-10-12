/** 
 * Copyright 2019-2023, Tomasz Żebrowski
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
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

		if (pid.getAlert().getUpperThreshold() != null && 
				doubleValue > pid.getAlert().getUpperThreshold().doubleValue()) {
			return MetricValidatorStatus.IN_ALERT_UPPER;
		}

		if (pid.getAlert().getLowerThreshold() != null && 
				doubleValue < pid.getAlert().getLowerThreshold().doubleValue()) {
			return MetricValidatorStatus.IN_ALERT_LOWER;
		}

		return MetricValidatorStatus.OK;
	}
}
