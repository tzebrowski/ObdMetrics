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

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class WorkflowOrchestrator {

	private static final int EXPECTED_THREADS_NUM = 3;

	private final ExecutorService workflowPool = new ThreadPoolExecutor(1, 1, 1L, TimeUnit.SECONDS,
			new SynchronousQueue<>());

	private final AtomicReference<Future<?>> currentTask = new AtomicReference<>();
	private final AtomicReference<Workflow> activeWorkflow = new AtomicReference<>();
	private static final WorkflowOrchestrator instance = new WorkflowOrchestrator();

	static WorkflowOrchestrator instance() {
		return instance;
	}

	WorkflowExecutionStatus submit(@NonNull Workflow workflow, Runnable task) {

		if (currentTask.get() != null && !currentTask.get().isDone()) {
			log.warn("Orchestrator rejected start request. A workflow is already running.");
			return WorkflowExecutionStatus.REJECTED;
		}

		activeWorkflow.set(workflow);

		final Future<?> future = workflowPool.submit(() -> {
			try {
				task.run();
			} catch (Exception e) {
				log.error("Workflow crashed", e);
			} finally {
				activeWorkflow.set(null);
			}
		});

		currentTask.set(future);
		return WorkflowExecutionStatus.STARTED;
	}

	boolean isRunning() {
		final Future<?> task = currentTask.get();
		return task != null && !task.isDone() && EXPECTED_THREADS_NUM == numberOfRunningThreads();
	}

	void stop() {
		
		final Future<?> task = currentTask.get();
		if (task != null) {
			task.cancel(true);
		}
	}

	private int numberOfRunningThreads() {
		final Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		int threadsNum = 0;
		for (final Thread t : threadSet.toArray(new Thread[threadSet.size()])) {
			if (t.getName().startsWith(NamedThreadFactory.WORKFLOW_THREADS_NAME)) {
				threadsNum++;
			}
		}
		return threadsNum;
	}
}