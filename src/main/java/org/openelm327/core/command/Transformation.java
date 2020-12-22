package org.openelm327.core.command;

public interface Transformation<T> {
	default T transform(String raw) {
		return null;
	}
}
