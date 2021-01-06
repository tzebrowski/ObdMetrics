package org.openobd2.core.workflow;

public interface State {
	static class DummyState implements State {
	}

	default void stopping() {
	}

	default void completed() {
	}

	default void starting() {
	}

	default void discarded() {
	}

	default void error() {
	}

}
