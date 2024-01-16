/** 
 * Copyright 2019-2024, Tomasz Żebrowski
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package org.obd.metrics.api;

import java.util.List;

import org.obd.metrics.alert.Alerts;
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
import lombok.Singular;

/**
 * {@link Workflow} is the main interface that expose the API of the framework.
 * It contains typical operations that allows to play with the OBD adapters
 * like:
 * <ul>
 * <li>Connecting to the Adapter</li>
 * <li>Disconnecting from the Adapter</li>
 * <li>Collecting OBD2 metrics</li>
 * <li>Obtaining statistics registry</li>
 * <li>Obtaining OBD2 PIDs/Sensor registry</li>
 * <li>Gets notifications about errors that appears during interaction with the
 * device.</li>
 * 
 * </ul>
 * 
 * @version 9.2.0
 * @see Adjustments
 * @see AdapterConnection
 * 
 * @since 0.0.1
 * @author tomasz.zebrowski
 */
public interface Workflow {

	/**
	 * Updates query for already running workflow
	 * 
	 * @param query       queried PID's (parameter is mandatory)
	 * @param init        init settings of the Adapter
	 * @param adjustments additional settings for process of collection the data
	 */
	WorkflowExecutionStatus updateQuery(@NonNull Query query, @NonNull Init init, @NonNull Adjustments adjustments);

	/**
	 * It starts the process of collecting the OBD metrics
	 * 
	 * @param connection the connection to the Adapter (parameter is mandatory)
	 * @param query      queried PID's (parameter is mandatory)
	 */
	default WorkflowExecutionStatus start(@NonNull AdapterConnection connection, @NonNull Query query) {
		return start(connection, query, Init.DEFAULT, Adjustments.DEFAULT);
	}

	/**
	 * It starts the process of collecting the OBD metrics
	 * 
	 * @param connection  the connection to the Adapter (parameter is mandatory)
	 * @param query       queried PID's (parameter is mandatory)
	 * @param adjustments additional settings for process of collection the data
	 */
	default WorkflowExecutionStatus start(@NonNull AdapterConnection connection, @NonNull Query query,
			Adjustments adjustments) {
		return start(connection, query, Init.DEFAULT, adjustments);
	}

	/**
	 * It starts the process of collecting the OBD metrics
	 * 
	 * @param adjustements additional settings for process of collection the data
	 * @param connection   the connection to the Adapter (parameter is mandatory)
	 * @param query        queried PID's (parameter is mandatory)
	 * @param init         init settings of the Adapter
	 */
	WorkflowExecutionStatus start(@NonNull AdapterConnection connection, @NonNull Query query, @NonNull Init init,
			Adjustments adjustements);

	/**
	 * Stops the current workflow.
	 */
	default void stop() {
		stop(true);
	}

	/**
	 * Stops the current workflow.
	 * 
	 * @param gracefulStop indicates whether workflow should be gracefully stopped.
	 */
	void stop(boolean gracefulStop);

	/**
	 * Informs whether {@link Workflow} process is already running.
	 * 
	 * @return true when process is already running.
	 */
	boolean isRunning();

	/**
	 * Rebuild {@link PidDefinitionRegistry} with new resources
	 * 
	 * @param pids new resources
	 */
	void updatePidRegistry(Pids pids);

	/**
	 * Gets the current pid registry for the workflow.
	 * 
	 * @return instance of {@link PidDefinitionRegistry}
	 */
	PidDefinitionRegistry getPidRegistry();

	/**
	 * Gets diagnostics collected during the session.
	 * 
	 * @return instance of {@link Diagnostics}
	 */
	Diagnostics getDiagnostics();

	/**
	 * Gets allerts collected during the session.
	 * 
	 * @return instance of {@link Alerts}
	 */
	Alerts getAlerts();

	/**
	 * It creates default {@link Workflow} implementation.
	 * 
	 * @param pids                   PID's configuration
	 * @param formulaEvaluatorConfig the instance of {@link FormulaEvaluatorConfig}.
	 *                               Might be null.
	 * @param observer               the instance of {@link ReplyObserver}
	 * @param lifecycleList          the instance of {@link Lifecycle}
	 * @return instance of {@link Workflow}
	 */
	@Builder(builderMethodName = "instance", buildMethodName = "initialize")
	static Workflow newInstance(Pids pids, FormulaEvaluatorConfig formulaEvaluatorConfig,
			@NonNull ReplyObserver<Reply<?>> observer, @Singular("lifecycle") List<Lifecycle> lifecycleList) {

		return new DefaultWorkflow(pids, formulaEvaluatorConfig, observer, lifecycleList);
	}
}