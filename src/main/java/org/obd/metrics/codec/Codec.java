package org.obd.metrics.codec;

public interface Codec<T> {
	
	default T decode(String raw) {
		return null;
	}
}
