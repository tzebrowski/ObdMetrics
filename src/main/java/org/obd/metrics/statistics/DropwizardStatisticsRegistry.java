package org.obd.metrics.statistics;

import org.obd.metrics.ObdMetric;
import org.obd.metrics.Reply;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.command.obd.SupportedPidsCommand;
import org.obd.metrics.pid.PidDefinition;

import com.codahale.metrics.MetricRegistry;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DropwizardStatisticsRegistry extends ReplyObserver implements StatisticsRegistry {

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
			log.debug("Failed to proceed request", e);
		}
	}

	@Override
	public MetricStatistics findBy(@NonNull PidDefinition pid) {
		return new DropwizardMetricsStatistics(metrics.histogram("hist." + pid.getId()).getSnapshot());
	}

	@Override
	public double getRatePerSec(@NonNull PidDefinition pid) {
		return metrics.meter("meter." + pid.getId()).getMeanRate();
	}
}
