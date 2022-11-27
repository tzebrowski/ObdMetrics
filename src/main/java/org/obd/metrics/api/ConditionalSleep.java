package org.obd.metrics.api;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;

@Builder
final class ConditionalSleep {
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
