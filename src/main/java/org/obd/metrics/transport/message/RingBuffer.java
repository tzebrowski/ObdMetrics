package org.obd.metrics.transport.message;

import java.util.ArrayList;
import java.util.List;

final class RingBuffer {

	final static RingBuffer instance = new RingBuffer();

	private int CAPACITY = 50;
	private final List<BytesMessage> items = new ArrayList<BytesMessage>(CAPACITY);
	private int position;

	RingBuffer() {
		for (int i = 0; i < CAPACITY; i++) {
			items.add(new BytesMessage());
		}
	}

	BytesMessage poll() {
		if (position == CAPACITY) {
			position = 0;
		}

		final BytesMessage rawMessage = items.get(position);
		position++;
		return rawMessage;
	}
}
