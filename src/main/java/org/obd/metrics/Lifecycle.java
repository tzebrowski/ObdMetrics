package org.obd.metrics;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

public interface Lifecycle {

	@Slf4j
	public static class LifeCycleSubscriber implements Lifecycle {

		private final List<Lifecycle> items = new ArrayList<Lifecycle>();

		public void subscribe(Lifecycle lifecycle) {
			if (lifecycle == null) {
				log.error("Specified lifecycle is null, skipping.");
			} else {
				if (this == lifecycle) {
					log.error("We do not want to register itself.");
				}else {
					items.add(lifecycle);
				}
			}
		}

		@Override
		public void onConnecting() {
			log.debug("Triggering event onConnecting");
			items.forEach(p -> p.onConnecting());
		}

		@Override
		public void onError(String message, Throwable e) {
			log.debug("Triggering event onError");
			items.forEach(p -> p.onError(message, e));
		}

		@Override
		public void onRunning(DeviceProperties properties) {
			log.debug("Triggering event onRunning");
			items.forEach(p -> p.onRunning(properties));
		}

		@Override
		public void onStopped() {
			log.debug("Triggering event onStopped");
			items.forEach(p -> p.onStopped());
		}

		@Override
		public void onStopping() {
			log.debug("Triggering event onStopping");
			items.forEach(p -> p.onStopping());
		}
	}

	default void onStopped() {
	}

	default void onStopping() {
	}

	default void onConnecting() {
	}

	default void onRunning(DeviceProperties properties) {
	}

	default void onError(String message, Throwable e) {
	}
}
