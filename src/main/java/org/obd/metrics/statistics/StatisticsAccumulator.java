package org.obd.metrics.statistics;

import org.obd.metrics.Reply;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.ObdMetric;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.obd.SupportedPidsCommand;

import com.codahale.metrics.MetricRegistry;

import lombok.NonNull;

public class StatisticsAccumulator extends ReplyObserver {
	private final MetricRegistry metrics = new MetricRegistry();

	@Override
	public void onNext(Reply reply) {

		var command = reply.getCommand();
		if (reply instanceof ObdMetric && !(command instanceof SupportedPidsCommand)) {
			// records just ObdCommand metrics
			var histogram = metrics.histogram("hist." + command.getQuery());
			histogram.update(((ObdMetric)reply).valueToLong());
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
