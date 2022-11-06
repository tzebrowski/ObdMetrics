package org.obd.metrics.codec.batch;

import org.obd.metrics.transport.message.ConnectorResponseFactory;

final class BatchMessageBuilder {
	static BatchMessage instance(byte[] message) {
		return new BatchMessage(null, ConnectorResponseFactory.wrap(message));
	}
}
