package org.obd.metrics.transport.message;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class CircularObjectPool<T> {

	private final int capacity;
	private final List<T> items;
	private AtomicInteger pos = new AtomicInteger(0);

	CircularObjectPool(Class<T> clazz, int capacity) {
		
		this.capacity = capacity;
		this.items = new ArrayList<T>(capacity);
		
		try {
			for (int i = 0; i < capacity; i++) {
				items.add(clazz.getDeclaredConstructor().newInstance());
			}
		} catch (InstantiationException | IllegalAccessException 
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			log.error("Failed to inititiate instance of class={}", clazz, e);
		}
	}

	T poll() {

		if (pos.get() >= capacity - 1) {
			pos.set(0);
		}
		return items.get(pos.getAndIncrement());
	}
}
