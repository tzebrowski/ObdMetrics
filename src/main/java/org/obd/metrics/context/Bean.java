package org.obd.metrics.context;

import java.util.NoSuchElementException;
import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Bean<T> {
	private final T value;

	public static <T> Bean<T> of(T value) {
		return new Bean<>(value);
	}

	public T get() {
		if (value == null) {
			throw new NoSuchElementException("No value present");
		}
		return value;
	}

	public void apply(Consumer<? super T> action) {
		if (value != null) {
			action.accept(value);
		}
	}

}