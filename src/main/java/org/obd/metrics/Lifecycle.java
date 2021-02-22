package org.obd.metrics;

public interface Lifecycle {
	
	static class DefaultLifecycle implements Lifecycle {
	}

	public static final DefaultLifecycle DEFAULT = new DefaultLifecycle();

	default void onStopped() {
	}

	default void onStopping() {
	}

	default void onConnecting() {
	}

	default void onConnected(DeviceProperties properties) {
	}
	
	default void onError(String message, Throwable e) {
	}
}
