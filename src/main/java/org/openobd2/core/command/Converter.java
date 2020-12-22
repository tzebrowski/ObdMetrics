package org.openobd2.core.command;

public interface Converter<T> {
	default T convert(String raw) {
		return null;
	}
}
