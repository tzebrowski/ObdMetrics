/** 
 * Copyright 2019-2024, Tomasz Żebrowski
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
package org.obd.metrics.api;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;

@Builder
public final class ConditionalSleep {
	private final TimeUnit timeUnit = TimeUnit.MILLISECONDS;

	@NonNull
	final Supplier<Boolean> condition;

	@NonNull
	final Long slice;

	@Default
	private boolean enabled = true;

	public long sleep(final long timeout) throws InterruptedException {
		if (enabled) {
			if (slice >= timeout) {
				timeUnit.sleep(timeout);
				return timeout;
			} else {

				final long inital = System.currentTimeMillis();

				long currentTime = 0;

				while (currentTime < timeout && !condition.get()) {

					long targetSleepTime = slice;
					currentTime = System.currentTimeMillis() - inital;
					if (currentTime + targetSleepTime >= timeout) {
						currentTime += (targetSleepTime = timeout - currentTime);
					}

					timeUnit.sleep(targetSleepTime);
				}

				return currentTime;
			}
		}
		timeUnit.sleep(timeout);
		return timeout;
	}
}
