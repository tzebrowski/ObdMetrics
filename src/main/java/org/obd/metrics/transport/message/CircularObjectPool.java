package org.obd.metrics.transport.message;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class CircularObjectPool<T> {

	private final int capacity;
	/** Underlying storage array. */
	private transient T[] elements;

	private AtomicInteger pos = new AtomicInteger(0);

	CircularObjectPool(final Class<T> clazz, final int capacity) {

		this.capacity = capacity;
		this.elements = (T[]) new Object[capacity];

		try {
			for (int i = 0; i < capacity; i++) {
				elements[i] = clazz.getDeclaredConstructor().newInstance();
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			log.error("Failed to inititiate instance of class={}", clazz, e);
		}
	}

	T poll() {

		if (pos.get() >= capacity - 1) {
			pos.set(0);
		}
		return elements[pos.getAndIncrement()];
	}
}
