 /**
 * Copyright 2019-2026, Tomasz Żebrowski
 *
 * <p>Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.obd.metrics.transport.mock.strategy;

import org.obd.metrics.pid.PidDefinition;

final class SmartSawtoothStrategy implements GeneratorStrategy {

	@Override
	public Double calculateNext(PidDefinition pid, Double currentValue) {
		double min = pid.getMin().doubleValue();
		final double max = pid.getMax().doubleValue();

		final double nextValue = currentValue + getIncrementStep(max);

		if (nextValue >= max) {
			return min; // Reset to start
		}
		return nextValue;
	}

	private double getIncrementStep(double maxValue) {
		if (maxValue < 2)
			return 0.005;
		if (maxValue < 5)
			return 0.05;
		if (maxValue <= 21 && maxValue >= 5)
			return 0.1;
		if (maxValue <= 100 && maxValue >= 22)
			return 1.0;
		if (maxValue <= 200 && maxValue >= 100)
			return 2.0;
		if (maxValue >= 1000)
			return 10.0;
		return 1.0;
	}
}