package org.obd.metrics.diagnostic;

import java.util.HashMap;
import java.util.Map;

import org.obd.metrics.ObdMetric;
import org.obd.metrics.pid.PidDefinition;

import com.dynatrace.dynahist.layout.Layout;
import com.dynatrace.dynahist.layout.LogQuadraticLayout;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class DefaultHistogramBuilder implements HistogramBuilder {
	
	private static final String HIST_KEY = "hist.";

	@RequiredArgsConstructor
	final class DefaultHistogram implements Histogram {
		private final com.dynatrace.dynahist.Histogram delegate;

		@Override
		public double getMax() {
			return normalize(delegate.getMax());
		}

		@Override
		public double getMin() {
			return normalize(delegate.getMin());
		}

		@Override
		public double getMean() {
			return normalize(delegate.getQuantile(0.5));
		}

		private double normalize(double value) {
			return value == Double.NaN || 
				   value == Double.NEGATIVE_INFINITY || 
				   value == Double.POSITIVE_INFINITY 
				   ? 0.0 : value;
		}
	}
	
	private final Map<String, com.dynatrace.dynahist.Histogram> hists = new HashMap<String, com.dynatrace.dynahist.Histogram>();

	void update(ObdMetric obdMetric) {
		final PidDefinition pidDefinition = obdMetric.getCommand().getPid();
		log.trace("Update histogram: {} {}", pidDefinition.getPid(), obdMetric.valueToDouble());
		getOrCreate(pidDefinition).addValue(obdMetric.valueToDouble());
	}

	@Override
	public Histogram findBy(PidDefinition pid) {
		return new DefaultHistogram(getOrCreate(pid));
	}

	void reset() {
		hists.clear();
	}

	private com.dynatrace.dynahist.Histogram getOrCreate(PidDefinition pid) {
		final String key = HIST_KEY + pid.getId();
		if (hists.containsKey(key)) {
			return hists.get(key);
		} else {
			final Layout layout = LogQuadraticLayout.create(1e-5, 1e-2, -1e9, 1e9);
			final com.dynatrace.dynahist.Histogram histogram = com.dynatrace.dynahist.Histogram
			        .createDynamic(layout);
			hists.put(key, histogram);
			return histogram;
		}
	}
}
