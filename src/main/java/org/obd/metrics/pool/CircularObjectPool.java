package org.obd.metrics.pool;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class CircularObjectPool<T> implements ObjectAllocator<T> {

	private final int capacity;
	/** Underlying storage array. */
	private transient T[] elements;

	private AtomicInteger pos = new AtomicInteger(0);

	@SuppressWarnings("unchecked")
	CircularObjectPool(final Class<T> clazz, final int capacity) {
		this.capacity = capacity;
		this.elements = (T[]) Array.newInstance(clazz, capacity);

		try {
			final Constructor<T> declaredConstructor = clazz.getDeclaredConstructor();
			declaredConstructor.setAccessible(true);
			
			for (int i = 0; i < capacity; i++) {
				elements[i] = declaredConstructor.newInstance();
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			log.error("Failed to inititiate instance of class={}", clazz, e);
		}
	}

	@Override
	public T allocate() {

		if (pos.get() >= capacity - 1) {
			pos.set(0);
		}
		return elements[pos.getAndIncrement()];
	}
}
