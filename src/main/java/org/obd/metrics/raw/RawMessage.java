package org.obd.metrics.raw;

import org.obd.metrics.pid.PidDefinition;

public interface RawMessage {

	static final RawMessage EMPTY_MESSAGE = MessagesFactory.instance.poll(new byte[] {}, 0, 0);

	byte[] getBytes();

	
	int getLength();

	void exctractDecimals(PidDefinition pid, DecimalReceiver decimalHandler);

	default byte[] copy() {
		return getBytes();
	}
	
	default boolean isCacheable() {
		return false;
	}

	default Long id() {
		return -1L;
	}

	default String getMessage() {
		return null;
	}

	default boolean isAnswerCodeSuccess(final byte[] expectedAnswer) {
		return true;
	}

	default boolean isEmpty() {
		return false;
	}

	default boolean isError() {
		return false;
	}

	static RawMessage wrap(final byte[] value, int from, int to) {
		return MessagesFactory.instance.poll(value, from, to);
	}

	static RawMessage wrap(final byte[] value) {
		return wrap(value, 0, value.length);
	}
}
