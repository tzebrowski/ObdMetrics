package org.obd.metrics.diagnostic;

import java.util.Optional;

import org.obd.metrics.pid.PidDefinition;

/**
 * Facade interface that provide diagnosis information about current state of
 * data collection.
 * 
 * @author tomasz.zebrowski
 */
public interface Diagnostics {

	Histogram findHistogramBy(PidDefinition pid);

	Optional<Rate> getRateBy(RateType rateType, PidDefinition pid);

	Optional<Rate> getRateBy(RateType rateType);

	void reset();

	static Diagnostics instance() {
		return new DefaultDiagnostics();
	}
}
