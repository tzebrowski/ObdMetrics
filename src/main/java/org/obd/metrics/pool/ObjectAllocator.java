package org.obd.metrics.pool;

public interface ObjectAllocator<T> {

	T allocate();

	static <F> ObjectAllocator<F> of(Class<F> clazz,int size){
		return new CircularObjectPool<F>(clazz, size);
	}
}