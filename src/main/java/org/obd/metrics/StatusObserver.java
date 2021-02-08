package org.obd.metrics;

public interface StatusObserver {
	
	static class DefaultStatusObserver implements StatusObserver {
	}

	public static final DefaultStatusObserver DEFAULT = new DefaultStatusObserver();

	default void onStopped() {
	}

	default void onStopping() {
	}

	default void onConnecting() {
	}

	default void onConnected() {
	}
	
	default void onError(String message, Throwable e) {
	}
}
