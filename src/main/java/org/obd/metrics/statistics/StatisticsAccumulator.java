package org.obd.metrics.statistics;

import org.obd.metrics.Metric;
import org.obd.metrics.MetricsObserver;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.obd.SupportedPidsCommand;

import com.codahale.metrics.MetricRegistry;

import lombok.NonNull;

public class StatisticsAccumulator extends MetricsObserver {
	private final MetricRegistry metrics = new MetricRegistry();

	@Override
	public void onNext(Metric<?> metric) {

		var command = metric.getCommand();
		if (command instanceof ObdCommand && !(command instanceof SupportedPidsCommand)) {
			// records just ObdCommand metrics
			var histogram = metrics.histogram("hist." + command.getQuery());
			histogram.update(metric.valueToLong());
			metrics.meter("meter." + command.getQuery()).mark();
		}
	}

	public Statistics findBy(@NonNull Command command) {
		var histogram = metrics.histogram("hist." + command.getQuery());
		return new DefaultStatistics(histogram.getSnapshot());
	}

	public double getRatePerSec(@NonNull Command command) {
		var meter = metrics.meter("meter." + command.getQuery());
		return meter.getMeanRate();
	}
}
