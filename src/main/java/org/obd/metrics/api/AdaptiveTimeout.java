package org.obd.metrics.api;

import java.util.Timer;
import java.util.TimerTask;

import org.obd.metrics.statistics.StatisticsRegistry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class AdaptiveTimeout {

	@AllArgsConstructor
	final class Task extends TimerTask {
		private final AdaptiveTimeoutPolicy policy;

		@Override
		public void run() {
			final double currentCommandFrequency = statisticsRegistry.getRandomRatePerSec();

			if (currentCommandFrequency == 0) {
				// did not start yet
			} else {
				log.info("Current RPS: {},requested RPS: {}, current timeout: {}",
				        currentCommandFrequency, policy.getCommandFrequency(),
				        currentTimeout);

				if (policy.isEnabled()) {
					if (currentCommandFrequency < policy.getCommandFrequency()) {
						if (currentTimeout > policy.getMinimumTimeout()) {
							long newTimeout = currentTimeout - 10;
							if (newTimeout < policy.getMinimumTimeout()) {
								newTimeout = policy.getMinimumTimeout();
							}
							log.info("Current RPS: {} is bellow requested: {}. Decreasing timeout to: {}",
							        currentCommandFrequency, policy.getCommandFrequency(), newTimeout);
							currentTimeout = newTimeout;
						} else {
							log.debug("Current timeout is bellow minimum value which is {}",
							        policy.getMinimumTimeout());
						}
					} else {
						// increase timeout if it is highly above expected throughput
					}
				}
			}
		}
	}

	private Timer timer = new Timer();
	private final AdaptiveTimeoutPolicy policy;

	@Getter
	private volatile long currentTimeout;

	private final Task task;
	private final StatisticsRegistry statisticsRegistry;

	AdaptiveTimeout(final AdaptiveTimeoutPolicy policy, final StatisticsRegistry statisticsRegistry) {
		this.policy = policy;
		this.task = new Task(policy);
		this.currentTimeout = 1000 / policy.getCommandFrequency();
		this.statisticsRegistry = statisticsRegistry;

		if (this.currentTimeout < policy.getMinimumTimeout()) {
			this.currentTimeout = policy.getMinimumTimeout();
		}

		log.info("Timeout: {}ms for expected command frequency: {}, "
		        + "adaptive timing enabled: {}, check interval: {}",
		        currentTimeout,
		        policy.getCommandFrequency(),
		        policy.isEnabled(),
		        policy.getCheckInterval());

	}

	void cancel() {
		if (policy.isEnabled()) {
			log.info("Canceling adaptive timeout task");
			task.cancel();
		}
	}

	void schedule() {
		if (policy.isEnabled()) {
			log.info("Scheduling adaptive timeout task. Fixed rate: {}", policy
			        .getCheckInterval());
			timer.scheduleAtFixedRate(task, 0, policy
			        .getCheckInterval());
		}
	}

}