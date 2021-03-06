package org.obd.metrics.api;

import org.obd.metrics.pid.PidRegistry;
import org.obd.metrics.statistics.StatisticsRegistry;

import lombok.NonNull;

/**
 * Thats is the main interface that expose the API of the framework. It contains
 * typical operations that allows to play with the OBD adapters like:
 * <ul>
 * <li>connecting to the device</li>
 * <li>collecting the the OBD metrics</li>
 * <li>gets notifications about errors that appears during interaction with the
 * device.</li>
 * </ul>
 * 
 * Typically instance of the Workflow is create by {@link WorkflowFactory}, see
 * it for details.
 * 
 * @see WorkflowFactory
 * @see WorkflowContext
 * 
 * @since 0.0.1
 * @author tomasz.zebrowski
 */
public interface Workflow {

	/**
	 * It starts the process of collecting the OBD metrics
	 * 
	 * @param context instance of the {@link WorkflowContext}
	 */
	void start(@NonNull WorkflowContext context);

	/**
	 * Stops the current workflow.
	 */
	void stop();

	/**
	 * Gets the current pid registry for the workflow.
	 * 
	 * @return instance of {@link PidRegistry}
	 */
	PidRegistry getPidRegistry();

	/**
	 * Gets statistics collected during the work.
	 * 
	 * @return statistics instance of {@link StatisticsRegistry}
	 */
	StatisticsRegistry getStatisticsRegistry();

}