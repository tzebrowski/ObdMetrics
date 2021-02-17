package org.obd.metrics.api;

import java.util.Set;

import org.obd.metrics.connection.Connection;
import org.obd.metrics.pid.PidRegistry;
import org.obd.metrics.statistics.StatisticsAccumulator;

public interface Workflow {

	void start();

	void stop();

	PidRegistry getPids();

	StatisticsAccumulator getStatistics();

	Workflow connection(Connection connection);

	Workflow filter(Set<Long> filter);

	Workflow batch(boolean batchEnabled);
}