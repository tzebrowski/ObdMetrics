package org.obd.metrics.transport.message;

import java.util.ArrayList;
import java.util.List;

final class RingBuffer {

	final static RingBuffer instance = new RingBuffer();

	private int CAPACITY = 50;
	private final List<BytesConnectorResponse> items = new ArrayList<BytesConnectorResponse>(CAPACITY);
	private int position;

	RingBuffer() {
		for (int i = 0; i < CAPACITY; i++) {
			items.add(new BytesConnectorResponse());
		}
	}

	BytesConnectorResponse poll() {
		if (position == CAPACITY) {
			position = 0;
		}

		final BytesConnectorResponse rawMessage = items.get(position);
		position++;
		return rawMessage;
	}
}
