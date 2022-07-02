package org.obd.metrics.api;

import java.util.Arrays;
import java.util.List;

import rx.Observer;

public abstract class ReplyObserver<T extends Reply<?>> implements Observer<T> {
	@Override
	public void onCompleted() {
	}

	@Override
	public void onError(Throwable e) {

	}

	public List<Class<?>> subscribeFor() {
		return Arrays.asList();
	}
}
