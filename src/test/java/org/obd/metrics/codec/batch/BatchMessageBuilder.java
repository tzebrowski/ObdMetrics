package org.obd.metrics.codec.batch;

final class BatchMessageBuilder {
	static BatchMessage instance(byte[] message) {
		return new BatchMessage(null, message);
	}
}
