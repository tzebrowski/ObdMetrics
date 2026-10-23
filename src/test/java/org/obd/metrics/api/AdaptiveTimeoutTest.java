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
package org.obd.metrics.api;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.diagnostic.Diagnostics;

public class AdaptiveTimeoutTest {

	private final AdaptiveTimeoutPolicy policy = AdaptiveTimeoutPolicy.builder().enabled(true).build();

	// The producer schedules the task on start, then pauses and resumes it around every DTC action:
	// rescheduling the same TimerTask threw "Task already scheduled or cancelled".
	@Test
	public void rescheduleAfterCancelTest() {
		final AdaptiveTimeout timeout = new AdaptiveTimeout(policy, Diagnostics.instance());
		timeout.schedule();
		timeout.cancel();

		Assertions.assertThatCode(timeout::schedule).doesNotThrowAnyException();
		timeout.cancel();
	}

	@Test
	public void scheduleTwiceTest() {
		final AdaptiveTimeout timeout = new AdaptiveTimeout(policy, Diagnostics.instance());
		timeout.schedule();

		Assertions.assertThatCode(timeout::schedule).doesNotThrowAnyException();
		timeout.cancel();
	}

	@Test
	public void cancelBeforeScheduleTest() {
		final AdaptiveTimeout timeout = new AdaptiveTimeout(policy, Diagnostics.instance());

		Assertions.assertThatCode(timeout::cancel).doesNotThrowAnyException();
	}
}
