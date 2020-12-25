package org.openobd2.core.converter;

public interface Converter<T> {
	
	default T convert(String raw) {
		return null;
	}
}
