package org.obd.metrics.api;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import lombok.Builder;
import lombok.NonNull;

@Builder
public final class ConditionalSleep {

	@NonNull
	final Supplier<Boolean> condition;

	@NonNull
	final Long particle;

	public long sleep(final long timeout) throws InterruptedException {

		final TimeUnit timeUnit = TimeUnit.MILLISECONDS;
		if (particle >= timeout) {
			timeUnit.sleep(timeout);
			return timeout;
		} else {

			final long inital = System.currentTimeMillis();

			long currentTime = 0;

			while (currentTime < timeout && !condition.get()) {

				long targetSleepTime = particle;
				currentTime = System.currentTimeMillis() - inital;
				if (currentTime + targetSleepTime >= timeout) {
					currentTime += (targetSleepTime = timeout - currentTime);
				}

				timeUnit.sleep(targetSleepTime);
			}

			return currentTime;
		}
	}
}
