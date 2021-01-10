package org.openobd2.core;

public interface StatusListener {
	
	static class DummyState implements StatusListener {
	}

	public static final DummyState DUMMY = new DummyState();

	default void onStopping() {
	}

	default void onComplete() {
	}

	default void onConnecting() {
	}

	default void onConnected() {
	}
	
	
	
	default void onError(String message) {
	}
}
