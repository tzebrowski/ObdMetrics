package org.obd.metrics.api;

import java.util.concurrent.TimeUnit;

import lombok.Builder;
import lombok.NonNull;

@Builder
final public class ConditionalSleep {

	private static final int MARGIN = 9;

	@FunctionalInterface
	static interface Condition {
		boolean quite();
	}

	@NonNull
	final Condition condition;

	@NonNull
	final Long waitTime;

	long sleep(final long timeout) {
		if (waitTime >= timeout) {
			try {
				TimeUnit.MILLISECONDS.sleep(timeout);
			} catch (InterruptedException e) {
			}
			return timeout;
		} else {
			long start = System.currentTimeMillis();
			long cnt = 0;
			do {
				try {
					TimeUnit.MILLISECONDS.sleep(waitTime);
				} catch (InterruptedException e) {
				}
				cnt = System.currentTimeMillis() - start;
			} while (cnt < (timeout - MARGIN) && !condition.quite());
			return cnt;
		}
	}
}
