package org.obd.metrics.raw;

import org.obd.metrics.pid.PidDefinition;

public interface RawMessage {
	static final DefaultRawMessage EMPTY_MESSAGE = new DefaultRawMessage(new byte[] {});

	byte[] getBytes();

	void toDecimals(PidDefinition pid, DecimalHandler decimalHandler);

	default boolean isCachable() {
		return false;
	}

	default Long id() {
		return -1L;
	}

	default String getMessage() {
		return null;
	}

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
