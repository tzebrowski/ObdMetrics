package org.obd.metrics.codec.batch.mapper;

import org.obd.metrics.transport.message.ConnectorResponseFactory;

public final class BatchMessageBuilder {
	public static BatchMessage instance(byte[] message) {
		return new BatchMessage(null, ConnectorResponseFactory.wrap(message));
	}
}
