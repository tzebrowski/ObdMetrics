package org.obd.metrics.api.model;

import java.util.HashSet;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

public interface Lifecycle {

	public final static Subscription subscription = new Subscription();

	@Slf4j
	public static final class Subscription implements Lifecycle {

		private final Set<Lifecycle> items = new HashSet<Lifecycle>();
		
		public void unregisterAll() {
			items.clear();
		}
		
		public void subscribe(Lifecycle lifecycle) {
			if (lifecycle == null) {
				log.debug("Specified lifecycle is null, skipping.");
			} else {
				if (this == lifecycle) {
					log.debug("We do not want to register itself.");
				} else {
					log.debug("Registering new lifecycle: '{}' listener", lifecycle.getClass().getSimpleName());
					items.add(lifecycle);
				}
			}
		}

		@Override
		public void onConnecting() {
			log.debug("Triggering event onConnecting");
			items.forEach(p -> {
				try {
					p.onConnecting();
				} catch (Exception e) {
					log.warn("Failed while executing onConnecting", e);
				}
			});
		}

		@Override
		public void onError(String message, Throwable e) {
			log.debug("Triggering event onError");
			items.forEach(p -> {
				try {
					p.onError(message, e);
				} catch (Exception ex) {
					log.warn("Failed while executing onError", e);
				}
			});
		}

		@Override
		public void onRunning(DeviceProperties properties) {
			log.debug("Triggering event onRunning");
			items.forEach(p -> {
				try {
					p.onRunning(properties);
				} catch (Exception ex) {
					log.warn("Failed while executing onRunning", ex);
				}
			});
		}

		@Override
		public void onStopped() {
			log.debug("Triggering event onStopped");
			items.forEach(p -> {
				try {
					p.onStopped();
				} catch (Exception ex) {
					log.warn("Failed while executing onStopped", ex);
				}
			});
		}

		@Override
		public void onStopping() {
			log.debug("Triggering event onStopping");
			items.forEach(p -> {
				try {
					p.onStopping();
				} catch (Exception ex) {
					log.warn("Failed while executing onStopping", ex);
				}
			});
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
