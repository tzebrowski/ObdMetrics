package org.obd.metrics.statistics;

import java.util.Optional;

import org.obd.metrics.pid.PidDefinition;

import lombok.Builder;
import lombok.NonNull;

public interface StatisticsRegistry {

	MetricStatistics findBy(@NonNull PidDefinition pid);

	double getRatePerSec(@NonNull PidDefinition pid);

	Optional<RatePerSec> getRatePerSec();
	
	void reset();
	
	@Builder
	static StatisticsRegistry newInstance() {
		return new DropwizardStatisticsRegistry();
	}
}
