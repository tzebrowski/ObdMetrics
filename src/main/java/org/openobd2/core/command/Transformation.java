package org.openobd2.core.command;

public interface Transformation<T> {
	default T transform(String raw) {
		return null;
	}
}
