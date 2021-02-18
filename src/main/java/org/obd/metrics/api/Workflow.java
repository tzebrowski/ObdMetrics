package org.obd.metrics.api;

import java.util.Set;

import org.obd.metrics.connection.Connection;
import org.obd.metrics.pid.PidRegistry;
import org.obd.metrics.statistics.StatisticsAccumulator;

/**
 * Thats is the main interface that expose the API of the framework. It contains typical operations
 * that allows to play with the OBD adapters like:
 * <ul>
 * <li>connecting to the device</li>
 * <li>collecting the the OBD metrics</li>
 * <li>gets  notifications about errors that appears during
 * interaction with the device.</li>
 * </ul>
 * 
 * Typically instance of the Workflow is create by {@link WorkflowFactory}, see it for details.
 * 
 * @see WorkflowFactory
 * @see Connection
 * 
 * @since 0.0.1
 * @author tomasz.zebrowski
 */
public interface Workflow {

	/**
	 * It starts the process of collecting of OBD metrics
	 * @param connection instenace of connection
	 */
	void start(Connection connection);

	/**
	 * It stops the workflow.
	 */
	void stop();

	/**
	 * Gets the current pid registry for the workflow.
	 * @return
	 */
	PidRegistry getPids();

	
	/**
	 * Gets statistics collected during work.
	 * @return statistics 
	 */
	StatisticsAccumulator getStatistics();

	/**
	 * Sets PID filter
	 * @param filter pid's id
	 */
	Workflow filter(Set<Long> filter);

	/**
	 * Enables batch commands.
	 * @param batchEnabled enables the batch
	 */
	Workflow batch(boolean batchEnabled);
}