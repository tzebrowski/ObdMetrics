package org.obd.metrics.model;

public interface RawMessage {

	String getMessage();

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

	static RawMessage instance(byte[] value) {
		return new DefaultRawMessage(value);
	}
}
