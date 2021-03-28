package org.obd.metrics.api;

import org.obd.metrics.connection.AdapterConnection;
import org.obd.metrics.pid.PidRegistry;
import org.obd.metrics.statistics.StatisticsRegistry;

import lombok.NonNull;

/**
 * {@link Workflow} is the main interface that expose the API of the framework.
 * It contains typical operations that allows to play with the OBD adapters
 * like:
 * <ul>
 * <li>Connecting to the Adapter</li>
 * <li>Disconnecting from the Adapter</li>
 * <li>Collecting the OBD metrics</li>
 * <li>Obtain statistics</li>
 * <li>Obtain pid's registry</li>
 * <li>Gets notifications about errors that appears during interaction with the
 * device.</li>
 * 
 * </ul>
 * 
 * Typically instance of the Workflow is create by {@link WorkflowFactory}, see
 * it for details.
 * 
 * @see WorkflowFactory
 * @see Adjustements
 * @see AdapterConnection
 * 
 * @since 0.0.1
 * @author tomasz.zebrowski
 */
public interface Workflow {

	/**
	 * It starts the process of collecting the OBD metrics
	 * 
	 * @param connection the connection to the Adapter (parameter is mandatory)
	 * @param query      queried PID's (parameter is mandatory)
	 */
	default void start(@NonNull AdapterConnection connection, @NonNull Query query) {
		start(connection, query, Adjustements.DEFAULT);
	}

	/**
	 * It starts the process of collecting the OBD metrics
	 * 
	 * @param adjustements additional settings for process of collection the data.
	 * @param connection   the connection to the Adapter (parameter is mandatory)
	 * @param query        queried PID's (parameter is mandatory)
	 */
	void start(@NonNull AdapterConnection connection, @NonNull Query query, Adjustements adjustements);

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