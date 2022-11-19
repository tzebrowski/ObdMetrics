package org.obd.metrics.pool;

public interface ObjectPool<T> {

	T poll();

	static <F> ObjectPool<F> of(Class<F> clazz,int size){
		return new CircularObjectPool<F>(clazz, size);
	}
}