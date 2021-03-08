package org.obd.metrics.statistics;

import com.codahale.metrics.Snapshot;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor
final class DropwizardMetricsStatistics implements MetricStatistics {
	@Delegate
	private final Snapshot delegate;
}