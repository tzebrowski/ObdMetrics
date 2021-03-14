package org.obd.metrics;

import rx.Observer;

public abstract class ReplyObserver<T extends Reply<?>> implements Observer<T> {
	@Override
	public void onCompleted() {
	}

	@Override
	public void onError(Throwable e) {

	}

	public String[] observables() {
		return new String[] {};
	}
}
