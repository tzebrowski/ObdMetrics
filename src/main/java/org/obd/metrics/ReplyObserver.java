package org.obd.metrics;

import rx.Observer;

public abstract class ReplyObserver implements Observer<Reply<?>> {
	@Override
	public void onCompleted() {
	}

	@Override
	public void onError(Throwable e) {

	}
}
