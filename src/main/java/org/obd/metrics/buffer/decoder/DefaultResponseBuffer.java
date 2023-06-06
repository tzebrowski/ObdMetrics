package org.obd.metrics.buffer.decoder;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DefaultResponseBuffer implements ResponseBuffer {
	private volatile LinkedBlockingDeque<Response> deque = new LinkedBlockingDeque<>();

	@Override
	public ResponseBuffer clear() {
		log.info("Invaldiating {} commands in the queue.", deque.size());
		deque.clear();
		return this;
	}

	@Override
	public ResponseBuffer addLast(Response command) {
		try {
			deque.putLast(command);
		} catch (final InterruptedException e) {
			log.warn("Failed to add command to the queue", e);
		}
		return this;
	}

	@Override
	public Response get() throws InterruptedException {
		return deque.takeFirst();
	}
}
