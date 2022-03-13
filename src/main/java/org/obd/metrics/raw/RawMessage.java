package org.obd.metrics.raw;

public interface RawMessage {

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
