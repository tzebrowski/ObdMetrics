package org.obd.metrics.pool;

public interface ObjectAllocator<T> {

	public static enum Strategy {
		Circular,
	}

	T allocate();

	static <F> ObjectAllocator<F> of(Strategy strategy, Class<F> clazz, int size) {
		switch (strategy) {
		case Circular:
			return new CircularObjectPool<F>(clazz, size);
		default:
			return new CircularObjectPool<F>(clazz, size);
		}
	}
}