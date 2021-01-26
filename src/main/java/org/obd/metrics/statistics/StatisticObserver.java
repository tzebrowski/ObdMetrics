package org.obd.metrics.statistics;

import org.obd.metrics.Metric;
import org.obd.metrics.MetricsObserver;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.obd.SupportedPidsCommand;
import org.obd.metrics.pid.PidRegistry;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StatisticObserver extends MetricsObserver {
	private final MetricRegistry metrics = new MetricRegistry();
	private final PidRegistry registry;

	public StatisticObserver(PidRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void onNext(Metric<?> metric) {
		var command = metric.getCommand();
		if (command instanceof ObdCommand && !(command instanceof SupportedPidsCommand)) {
			// records just ObdCommand metrics
			var histogram = metrics.histogram(command.getQuery());
			histogram.update(metric.valueToInt());
			metric.updateStatistics(new DefaultStatistics(histogram.getSnapshot()));
		}
	}

	public Histogram findBy(@NonNull String pid) {
		var pidDef = registry.findBy(pid);
		if (null == pidDef) {
			log.error("No historgam found for pid: {}", pid);
			return null;
		} else {
			return metrics.histogram(pidDef.getMode() + pidDef.getPid());
		}
	}
}
