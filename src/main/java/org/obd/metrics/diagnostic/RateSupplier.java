package org.obd.metrics.diagnostic;

import java.util.Optional;

import org.obd.metrics.pid.PidDefinition;

public interface RateSupplier {

	Optional<Rate> findBy(RateType rateType, PidDefinition pid);
	Optional<Rate> findBy(RateType rateType);
}