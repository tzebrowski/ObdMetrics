package org.obd.metrics.api;

import java.util.concurrent.TimeUnit;

import lombok.Builder;
import lombok.NonNull;

@Builder
final public class ConditionalSleep {

	@FunctionalInterface
	static interface Condition {
		boolean isMeet();
	}

	@NonNull
	final Condition condition;

	@NonNull
	final Long sleepTime;

	void sleep(final long timeout) throws InterruptedException {

		if (sleepTime >= timeout) {
			TimeUnit.MILLISECONDS.sleep(timeout);
		} else {
			final long startTime = System.currentTimeMillis();
			long currentTime = 0;
			do {
				long targetSleepTime = sleepTime;
				currentTime = System.currentTimeMillis() - startTime;
				if (currentTime + targetSleepTime >= timeout) {
					currentTime += (targetSleepTime = timeout - currentTime);
				}

				TimeUnit.MILLISECONDS.sleep(targetSleepTime);

			} while (currentTime < timeout && !condition.isMeet());
		}
	}
}
