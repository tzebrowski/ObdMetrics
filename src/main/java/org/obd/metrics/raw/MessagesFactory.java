package org.obd.metrics.raw;

import java.util.ArrayList;
import java.util.List;

final class MessagesFactory {

	final static MessagesFactory instance = new MessagesFactory();

	private int CAPACITY = 50;
	private final List<BytesRawMessage> items = new ArrayList<BytesRawMessage>(CAPACITY);
	private int position;

	MessagesFactory() {
		for (int i = 0; i < CAPACITY; i++) {
			items.add(new BytesRawMessage());
		}
	}

	RawMessage poll(final byte[] value, int start, int length) {
		if (position == CAPACITY) {
			position = 0;
		}

		final BytesRawMessage rawMessage = items.get(position);
		position++;
		rawMessage.update(value, start, length);
		return rawMessage;
	}
}
