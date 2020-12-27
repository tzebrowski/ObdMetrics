package org.openobd2.core.codec;

public interface Codec<T> {
	
	default T decode(String raw) {
		return null;
	}
}
