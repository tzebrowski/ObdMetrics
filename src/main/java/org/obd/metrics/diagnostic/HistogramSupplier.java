package org.obd.metrics.diagnostic;

import org.obd.metrics.pid.PidDefinition;

public interface HistogramSupplier {
	Histogram findBy(PidDefinition pid);
}