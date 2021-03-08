package org.obd.metrics.api;

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import lombok.Builder;
import lombok.NonNull;

@Builder
final class ConditionalSleep {

	@NonNull
	final BooleanSupplier condition;

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

			} while (currentTime < timeout && !condition.getAsBoolean());
		}
	}
}
