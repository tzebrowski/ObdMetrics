package org.openobd2.core;

public interface StatusObserver {
	
	static class DummyObserver implements StatusObserver {
	}

	public static final DummyObserver DUMMY = new DummyObserver();

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
