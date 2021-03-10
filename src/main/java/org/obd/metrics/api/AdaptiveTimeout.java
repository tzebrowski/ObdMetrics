package org.obd.metrics.api;

import java.time.Duration;
import java.time.Instant;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class AdaptiveTimeout {

	private Instant start = Instant.now();

	@Getter
	private long currentTimeout;

	private final long targetCommandFrequency;

	private final long checkInterval;

	private final long minimumTimeout;

	private final boolean enabled;

	AdaptiveTimeout(boolean enabled, long targetCommandFrequency,
	        long commandFrequencyCheckInterval,
	        long minimumTimeout) {

		this.targetCommandFrequency = targetCommandFrequency;
		this.checkInterval = commandFrequencyCheckInterval;
		this.minimumTimeout = minimumTimeout;
		this.currentTimeout = 1000 / targetCommandFrequency;
		this.enabled = enabled;
	}

	void update(double currentCommandFrequency) {
		if (enabled) {
			if (Duration.between(start, Instant.now()).toMillis() >= checkInterval) {

				log.info("Current RPS: {},requested RPS: {}, current timeout: {}",
				        currentCommandFrequency, targetCommandFrequency,
				        currentTimeout);

				if (currentCommandFrequency < targetCommandFrequency) {
					if (currentTimeout > minimumTimeout) {
						long newTimeout = currentTimeout - 10;
						if (newTimeout < minimumTimeout) {
							newTimeout = minimumTimeout;
						}
						log.info("Current RPS is bellow requested. Decreasing timeout to: {}", newTimeout);
						currentTimeout = newTimeout;
					} else {
						log.info("Current timeout is bellow minimum value which is {}",
						        minimumTimeout);
					}
				}

				start = Instant.now();
			}
		}
	}
}