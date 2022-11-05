package org.obd.metrics.transport.message;

import org.obd.metrics.pid.PidDefinition;

public interface ConnectorResponse {

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

	static ConnectorResponse wrap(final byte[] value, int from, int to) {
		final BytesMessage message = RingBuffer.instance.poll();
		message.update(value, from, to);
		return message;
	}

	static ConnectorResponse wrap(final byte[] value) {
		return wrap(value, 0, value.length);
	}
}
