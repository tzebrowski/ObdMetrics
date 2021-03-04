package org.obd.metrics.statistics;

import org.obd.metrics.ObdMetric;
import org.obd.metrics.Reply;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.command.obd.SupportedPidsCommand;
import org.obd.metrics.pid.PidDefinition;

import com.codahale.metrics.MetricRegistry;

import lombok.NonNull;

public class StatisticsAccumulator extends ReplyObserver {

	private final MetricRegistry metrics = new MetricRegistry();

	@Override
	public void onNext(Reply<?> reply) {
		try {
			var command = reply.getCommand();
			if (reply instanceof ObdMetric && !(command instanceof SupportedPidsCommand)) {
				// records just ObdCommand metrics
				var obdMetric = (ObdMetric) reply;
				var histogram = metrics.histogram("hist." + obdMetric.getCommand().getPid().getId());
				histogram.update(obdMetric.valueToLong());
				metrics.meter("meter." + obdMetric.getCommand().getPid().getId()).mark();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public Statistics findBy(@NonNull PidDefinition pid) {
		var histogram = metrics.histogram("hist." + pid.getId());
		return new DefaultStatistics(histogram.getSnapshot());
	}

	public double getRatePerSec(@NonNull PidDefinition pid) {
		var meter = metrics.meter("meter." + pid.getId());
		return meter.getMeanRate();
	}
}
