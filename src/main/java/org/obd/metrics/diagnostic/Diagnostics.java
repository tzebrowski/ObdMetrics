package org.obd.metrics.diagnostic;

import java.util.Optional;

import org.obd.metrics.pid.PidDefinition;

public interface Diagnostics {

	Histogram findHistogramBy(PidDefinition pid);

	double getRateBy(RateType rateType, PidDefinition pid);

	Optional<Rate> getRateBy(RateType rateType);

	void reset();

	static Diagnostics instance() {
		return new DropwizardDiagnostics();
	}
}
