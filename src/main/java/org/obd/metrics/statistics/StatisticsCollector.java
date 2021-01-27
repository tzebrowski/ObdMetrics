package org.obd.metrics.statistics;

import org.obd.metrics.Metric;
import org.obd.metrics.MetricsObserver;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.obd.SupportedPidsCommand;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;

import lombok.NonNull;

public class StatisticsCollector extends MetricsObserver {
	private final MetricRegistry metrics = new MetricRegistry();

	@Override
	public void onNext(Metric<?> metric) {
		var command = metric.getCommand();
		if (command instanceof ObdCommand && !(command instanceof SupportedPidsCommand)) {
			// records just ObdCommand metrics
			var histogram = metrics.histogram(command.getQuery());
			histogram.update(metric.valueToInt());
		}
	}

	public MetricStatistics findBy(@NonNull Command command) {
		final Histogram histogram = metrics.histogram(command.getQuery());
		return new DefaultStatistics(histogram.getSnapshot());
	}
}
