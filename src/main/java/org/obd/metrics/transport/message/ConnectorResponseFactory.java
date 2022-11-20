package org.obd.metrics.transport.message;

import org.obd.metrics.pool.ObjectAllocator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class ConnectorResponseFactory {
	
	private static final BytesConnectorResponse EMPTY_CONNECTOR_RESPONSE = new BytesConnectorResponse(0);
	
	private final static ObjectAllocator<BytesConnectorResponse> allocator = 
			ObjectAllocator.of(BytesConnectorResponse.class, 255);

	public static ConnectorResponse wrap(final byte[] value, int from, int to) {
		final BytesConnectorResponse message = allocator.allocate();
		message.update(value, from, to);
		return message;
	}

	public static ConnectorResponse wrap(final byte[] value) {
		return wrap(value, 0, value.length);
	}
	
	public static ConnectorResponse empty() {
		return EMPTY_CONNECTOR_RESPONSE;
	}
}
