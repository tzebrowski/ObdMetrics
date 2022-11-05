package org.obd.metrics.transport.message;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class ConnectorResponseFactory {

	private final static CircularObjectPool<BytesConnectorResponse> pool = 
			new CircularObjectPool<BytesConnectorResponse>(
			BytesConnectorResponse.class, 50);

	public static ConnectorResponse wrap(final byte[] value, int from, int to) {
		final BytesConnectorResponse message = pool.poll();
		message.update(value, from, to);
		return message;
	}

	public static ConnectorResponse wrap(final byte[] value) {
		return wrap(value, 0, value.length);
	}
}
