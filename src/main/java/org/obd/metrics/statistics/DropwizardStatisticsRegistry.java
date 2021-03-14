package org.obd.metrics.statistics;

import org.obd.metrics.ObdMetric;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.command.obd.SupportedPidsCommand;
import org.obd.metrics.pid.PidDefinition;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DropwizardStatisticsRegistry extends ReplyObserver<ObdMetric> implements StatisticsRegistry {

	private final MetricRegistry metrics = new MetricRegistry();

	@Override
	public void onNext(ObdMetric obdMetric) {
		try {
			var command = obdMetric.getCommand();
			if (!(command instanceof SupportedPidsCommand)) {
				var histogram = findHistogramBy(obdMetric.getCommand().getPid());
				histogram.update(obdMetric.valueToLong());
				findMeterBy(obdMetric.getCommand().getPid()).mark();
			}
		} catch (Throwable e) {
			log.info("Failed to proceed the request", e);
		}
	}

	@Override
	public MetricStatistics findBy(@NonNull PidDefinition pid) {
		return new DropwizardMetricsStatistics(findHistogramBy(pid).getSnapshot());
	}

	@Override
	public double getRatePerSec(@NonNull PidDefinition pid) {
		return findMeterBy(pid).getMeanRate();
	}

	private Meter findMeterBy(PidDefinition pid) {
		return metrics.meter("meter." + pid.getId());
	}

	private Histogram findHistogramBy(PidDefinition pid) {
		return metrics.histogram("hist." + pid.getId());
	}
}
