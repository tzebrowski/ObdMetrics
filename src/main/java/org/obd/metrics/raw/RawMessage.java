package org.obd.metrics.raw;

public interface RawMessage {
	static final DefaultRawMessage EMPTY_MESSAGE = new DefaultRawMessage(new byte[] {});

	default boolean isCachable() {
		return false;
	}

	default Long id() {
		return -1L;
	}

	default String getMessage() {
		return null;
	}

	byte[] getBytes();

	default boolean isAnswerCodeSuccess(byte[] expectedAnswer) {
		return true;
	}

	default boolean isEmpty() {
		return false;
	}

	default boolean isError() {
		return false;
	}

	static RawMessage wrap(byte[] value) {
		return new DefaultRawMessage(value);
	}
}
