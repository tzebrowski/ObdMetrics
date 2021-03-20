package org.obd.metrics.statistics;

import org.obd.metrics.pid.PidDefinition;

import lombok.Builder;
import lombok.NonNull;

public interface StatisticsRegistry {

	MetricStatistics findBy(@NonNull PidDefinition pid);

	double getRatePerSec(@NonNull PidDefinition pid);

	double getRandomRatePerSec();

	@Builder
	static StatisticsRegistry newInstance() {
		return new DropwizardStatisticsRegistry();
	}
}
