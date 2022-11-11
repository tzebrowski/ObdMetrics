package org.obd.metrics.transport.message;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class CircularObjectPool<T> {

	private final int capacity;
	private final List<T> items;
	private int position;

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
		if (position == capacity) {
			position = 0;
		}

		final T message = items.get(position);
		position++;
		return message;
	}
}
