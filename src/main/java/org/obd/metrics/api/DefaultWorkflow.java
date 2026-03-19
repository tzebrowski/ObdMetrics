 /**
 * Copyright 2019-2026, Tomasz Żebrowski
 *
 * <p>Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.obd.metrics.api;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.obd.metrics.alert.Alerts;
import org.obd.metrics.api.WorkflowOrchestrator.Task;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.DtcAction;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Lifecycle;
import org.obd.metrics.api.model.Pids;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.api.model.SniffingPolicy;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.diagnostic.Diagnostics;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.AdapterConnection;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DefaultWorkflow implements Workflow {

	private PidDefinitionRegistry registry;
	private final ExecutionContextFactory contextFactory;
	private volatile ExecutionContext activeContext;

	protected DefaultWorkflow(Pids pids, FormulaEvaluatorConfig formulaEvaluatorConfig,
			ReplyObserver<Reply<?>> eventsObserver, List<Lifecycle> lifecycle) {

		log.info("Creating an instance of the Workflow task.");
		updatePidRegistry(pids);
		
		this.contextFactory = new ExecutionContextFactory(
				registry, formulaEvaluatorConfig, eventsObserver, lifecycle);
	}

	@Override
	public Diagnostics getDiagnostics() {
		return activeContext.getDiagnostics();
	}

	@Override
	public Alerts getAlerts() {
		return activeContext.getAlerts();
	}
	
	@Override
	public void updatePidRegistry(Pids pids) {
		long tt = System.currentTimeMillis();
		try (final Resources sources = Resources.convert(pids)) {
			this.registry = PidDefinitionRegistry.builder().sources(sources.getResources()).build();
		}
		tt = System.currentTimeMillis() - tt;
		log.info("Loading resources files took: {}ms.", tt);
	}

	@Override
	public PidDefinitionRegistry getPidRegistry() {
		return registry;
	}

	@Override
	public boolean isRunning() {
		return WorkflowOrchestrator.instance().isRunning(this);
	}

	@Override
	public void stop(boolean gracefulStop) {
		log.info("Stopping workflow process...");
		if (activeContext != null) {
			activeContext.stop(gracefulStop);
		}
		WorkflowOrchestrator.instance().stop(this);
	}

	@Override
	public WorkflowExecutionStatus scheduleDTCAction(final Set<DtcAction> actions) {
		log.info("[DTC] Scheduling DTC action: {}", actions);

		if (isRunning() && activeContext != null) {
			activeContext.scheduleDTCAction(actions, registry);
			return WorkflowExecutionStatus.DTC_QUEUED;
		} else {
			log.warn("[DTC] No workflow is running.");
			return WorkflowExecutionStatus.NOT_RUNNING;
		}
	}

	@Override
	public WorkflowExecutionStatus executeRoutine(@NonNull Long routineId, @NonNull Init init) {
		log.info("[Routine] Executing routine: {}", routineId);
		log.info("[Routine] Protocol: {}, headers: {}", init.getProtocol(), init.getHeaders());

		if (isRunning() && activeContext != null) {
			return activeContext.executeRoutine(routineId, init, registry);
		} else {
			log.warn("[Routine] No workflow is running");
			return WorkflowExecutionStatus.NOT_RUNNING;
		}
	}

	@Override
	public WorkflowExecutionStatus updateQuery(@NonNull Query query, @NonNull Init init, @NonNull Adjustments adjustments) {
		long ts = System.currentTimeMillis();
		log.info("[Update] Updating running workflow with new query");
		log.info("[Update] Selected PID's: {}", query.getPids());

		if (isRunning() && activeContext != null) {
			new WorkflowBufferInitializer(CommandsBuffer.instance(), registry).debugPIDs(query, init, adjustments);
			activeContext.updateQuery(query, init, adjustments, registry);
			
			ts = System.currentTimeMillis() - ts;
			log.info("Workflow update operation took: {}ms", ts);
			return WorkflowExecutionStatus.UPDATED;
		} else {
			log.warn("No workflow is running");
			return WorkflowExecutionStatus.NOT_RUNNING;
		}
	}

	@Override
	public WorkflowExecutionStatus start(@NonNull AdapterConnection connection, @NonNull Init init,
			@NonNull Adjustments adjustments, SniffingPolicy sniffingPolicy) {
		return startInternal(connection, init, adjustments, Query.builder().build(), sniffingPolicy);
	}

	@Override
	public WorkflowExecutionStatus start(@NonNull AdapterConnection connection, @NonNull Query query,
			@NonNull Init init, @NonNull Adjustments adjustments) {
		return startInternal(connection, init, adjustments, query, null);
	}

	private WorkflowExecutionStatus startInternal(AdapterConnection connection, Init init,
			Adjustments adjustments, Query query, SniffingPolicy sniffingPolicy) {

		final Task task = (final ExecutorService executorService) -> {
			try {
				log.info("[Start] Starting workflow task.");

				this.activeContext = contextFactory.build(connection, init, adjustments, query, sniffingPolicy);
				final WorkflowBufferInitializer initializer = new WorkflowBufferInitializer(this.activeContext.getCommandsBuffer(), registry);
				initializer.debugPIDs(query, init, adjustments);
				initializer.initialize(init, adjustments, sniffingPolicy);
				
				
				log.info("[Start] Context initialized successfully. Invoking worker threads.");

				executorService.invokeAll(activeContext.getWorkerThreads());

			} catch (InterruptedException e) {
				log.info("Process was interrupted.");
			} catch (Throwable e) {
				log.error("Failed to initialize the Workflow task.", e);
			} finally {
				try {
					log.info("Stopping the Workflow task.");
					if (activeContext != null) {
						activeContext.getSubscription().onStopped();
					}
					executorService.shutdown();
				} catch (Throwable e) {
					log.error("Error occurred while stopping the workflow.", e);
				}
			}
		};

		return WorkflowOrchestrator.instance().submit(this, task);
	}
}