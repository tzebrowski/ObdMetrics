package org.obd.metrics;

import rx.Observer;

public abstract class MetricsObserver implements Observer<Metric<?>> {
	@Override
	public void onCompleted() {
	}

	@Override
	public void onError(Throwable e) {

	}

	@Override
	public void onNext(Metric<?> t) {

	}
}
