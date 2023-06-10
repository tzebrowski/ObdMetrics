package org.obd.metrics.buffer.decoder;

import java.util.concurrent.LinkedBlockingDeque;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DefaultConnectorResponseBuffer implements ConnectorResponseBuffer {
	private volatile LinkedBlockingDeque<ConnectorResponseWrapper> deque = new LinkedBlockingDeque<>();

	@Override
	public ConnectorResponseBuffer clear() {
		log.info("Invaldiating {} commands in the queue.", deque.size());
		deque.clear();
		return this;
	}

	@Override
	public ConnectorResponseBuffer addLast(ConnectorResponseWrapper command) {
		try {
			deque.putLast(command);
		} catch (final InterruptedException e) {
			log.warn("Failed to add command to the queue", e);
		}
		return this;
	}

	@Override
	public ConnectorResponseWrapper get() throws InterruptedException {
		return deque.takeFirst();
	}
}
