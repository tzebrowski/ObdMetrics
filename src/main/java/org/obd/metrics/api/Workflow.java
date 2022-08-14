package org.obd.metrics.api;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Lifecycle;
import org.obd.metrics.api.model.Pids;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
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
 * @version 4.0.0
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
		start(connection, query, Init.DEFAULT, Adjustments.DEFAULT);
	}

	/**
	 * It starts the process of collecting the OBD metrics
	 * 
	 * @param connection  the connection to the Adapter (parameter is mandatory)
	 * @param query       queried PID's (parameter is mandatory)
	 * @param adjustments additional settings for process of collection the data
	 */
	default void start(@NonNull AdapterConnection connection, @NonNull Query query, Adjustments adjustments) {
		start(connection, query, Init.DEFAULT, adjustments);
	}

	/**
	 * It starts the process of collecting the OBD metrics
	 * 
	 * @param adjustements additional settings for process of collection the data
	 * @param connection   the connection to the Adapter (parameter is mandatory)
	 * @param query        queried PID's (parameter is mandatory)
	 * @param init         init settings of the Adapter
	 */
	void start(@NonNull AdapterConnection connection, @NonNull Query query, @NonNull Init init,
	        Adjustments adjustements);

	/**
	 * Stops the current workflow.
	 * @param gracefulStop indicate whether workflow should be gracefully stopped.
	 */
	void stop(boolean gracefulStop);
	
	/**
	 * Stops the current workflow.
	 */
	default void stop() {
		stop(true);
	}

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
	 * It creates default {@link Workflow} implementation.
	 * 
	 * @param pids           PID's configuration
	 * @param formulaEvaluatorConfig the instance of {@link FormulaEvaluatorConfig}. Might be null.
	 * @param observer       the instance of {@link ReplyObserver}
	 * @param lifecycle      the instance of {@link Lifecycle}
	 * @return instance of {@link Workflow}
	 */
	@Builder(builderMethodName = "instance", buildMethodName = "initialize")
	static Workflow newInstance(Pids pids, FormulaEvaluatorConfig formulaEvaluatorConfig,
	        @NonNull ReplyObserver<Reply<?>> observer, Lifecycle lifecycle) {

		return new DefaultWorkflow(pids, formulaEvaluatorConfig, observer, lifecycle);
	}
}