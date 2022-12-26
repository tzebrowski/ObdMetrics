package org.obd.metrics.api;

import org.assertj.core.api.Assertions;
import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.api.model.ReplyObserver;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class RawConnectorReponseSizeChecker extends ReplyObserver<ObdMetric> {
	long callCount;
	final int expectedSize;
	
	@Override
	public void onNext(ObdMetric t) {
		Assertions.assertThat(t.getRaw().capacity()).isEqualTo(expectedSize);
		callCount++;
	}
}