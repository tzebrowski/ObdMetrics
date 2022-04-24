package org.obd.metrics.api;

import java.io.IOException;

import org.obd.metrics.Lifecycle;
import org.obd.metrics.Reply;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.diagnostic.Diagnostics;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.AdapterConnection;

import lombok.Builder;
import lombok.NonNull;

/**
 * {@link Workflow} is the main interface that expose the API of the framework.
 * It contains typical operations that allows to play with the OBD adapters
 * like:
 * <ul>
 * <li>Connecting to the Adapter</li>
 * <li>Disconnecting from the Adapter</li>
 * <li>Collecting the OBD metrics</li>
 * <li>Obtain statistics registry</li>
 * <li>Obtain pid's registry</li>
 * <li>Gets notifications about errors that appears during interaction with the
 * device.</li>
 * 
 * </ul>
 * 
 * Typically instance of the Workflow is create by {@link WorkflowFactory}, see
 * it for details.
 * 
 * @see Adjustments
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
		start(connection, query, Adjustments.DEFAULT);
	}

	/**
	 * It starts the process of collecting the OBD metrics
	 * 
	 * @param adjustements additional settings for process of collection the data.
	 * @param connection   the connection to the Adapter (parameter is mandatory)
	 * @param query        queried PID's (parameter is mandatory)
	 */
	void start(@NonNull AdapterConnection connection, @NonNull Query query, Adjustments adjustements);

	/**
	 * Stops the current workflow.
	 */
	void stop();

	/**
	 * Gets the current pid registry for the workflow.
	 * 
	 * @return instance of {@link PidDefinitionRegistry}
	 */
	PidDefinitionRegistry getPidRegistry();

	/**
	 * Gets diagnostics collected during the work.
	 * 
	 * @return instance of {@link Diagnostics}
	 */
	Diagnostics getDiagnostics();

	/**
	 * It creates different {@link Workflow} implementation.
	 */
	@Builder(builderMethodName = "instance", buildMethodName = "initialize")
	static Workflow newInstance(InitConfiguration init, Pids pids, String equationEngine,
	        @NonNull ReplyObserver<Reply<?>> observer, Lifecycle lifecycle)
	        throws IOException {

		return new DefaultWorkflow(init, pids, equationEngine, observer, lifecycle);
	}
}