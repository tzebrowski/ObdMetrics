package org.openobd2.core.workflow;

public interface State {
	
	static class DummyState implements State {
	}

	public static final DummyState DUMMY = new DummyState();

	default void onStopping() {
	}

	default void onComplete() {
	}

	default void onStarting() {
	}

	default void onDiscarded() {
	}

	default void onError() {
	}
}
