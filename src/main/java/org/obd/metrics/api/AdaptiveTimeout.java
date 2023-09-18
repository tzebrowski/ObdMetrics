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
package org.obd.metrics.api;

import java.util.Timer;
import java.util.TimerTask;

import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.diagnostic.Diagnostics;
import org.obd.metrics.diagnostic.RateType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class AdaptiveTimeout {

	@AllArgsConstructor
	final class Task extends TimerTask {

		@Override
		public void run() {
			diagnostics.rate().findBy(RateType.MEAN).ifPresent(currentCommandRate -> {
				if (log.isTraceEnabled()) {
					log.trace("Pid: {}, current RPS: {}, requested RPS: {}, current timeout: {}",
							currentCommandRate.getKey(), currentCommandRate.getValue(), policy.getCommandFrequency(),
							currentTimeout);
				}

				if (policy.isEnabled()) {
					if (currentCommandRate.getValue() < policy.getCommandFrequency()) {
						// decrease timeout if RPS is highly bellow expected throughput

						if (currentTimeout > policy.getMinimumTimeout()) {
							long newTimeout = currentTimeout - 10;
							if (newTimeout < policy.getMinimumTimeout()) {
								newTimeout = policy.getMinimumTimeout();
							}
							log.debug("Pid: {}, current RPS: {} is bellow requested: {}. Decreasing timeout to: {}",
									currentCommandRate.getKey(), currentCommandRate.getValue(),
									policy.getCommandFrequency(), newTimeout);

							currentTimeout = newTimeout;
						} else {
							log.debug("Pid: {},current timeout is bellow minimum value which is {}",
									currentCommandRate.getKey(), policy.getMinimumTimeout());
						}
					} else {
						// increase timeout if RPS is highly above expected throughput
					}
				}
			});

		}
	}

	private Timer timer = new Timer();
	private final AdaptiveTimeoutPolicy policy;

	@Getter
	private volatile long currentTimeout;

	private final Task task;
	private final Diagnostics diagnostics;

	AdaptiveTimeout(final AdaptiveTimeoutPolicy policy, final Diagnostics diagnostics) {
		this.policy = policy;
		this.task = new Task();
		this.currentTimeout = 1000 / policy.getCommandFrequency();
		this.diagnostics = diagnostics;

		if (this.currentTimeout < policy.getMinimumTimeout()) {
			this.currentTimeout = policy.getMinimumTimeout();
		}

		log.info(
				"Timeout: {}ms for expected command frequency: {}, "
						+ "adaptive timing enabled: {}, check interval: {}",
				currentTimeout, policy.getCommandFrequency(), policy.isEnabled(), policy.getCheckInterval());

	}

	void cancel() {
		if (policy.isEnabled()) {
			log.info("Canceling adaptive timeout task");
			task.cancel();
		}
	}

	void schedule() {
		if (policy.isEnabled()) {
			log.info("Scheduling adaptive timeout task. Fixed rate: {}", policy.getCheckInterval());
			timer.scheduleAtFixedRate(task, 0, policy.getCheckInterval());
		}
	}
}