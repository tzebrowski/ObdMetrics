package org.obd.metrics.diagnostic;

import org.obd.metrics.pid.PidDefinition;

public interface HistogramBuilder {
	Histogram findBy(PidDefinition pid);
}