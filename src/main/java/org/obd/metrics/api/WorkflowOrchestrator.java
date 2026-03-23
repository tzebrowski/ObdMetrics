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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class WorkflowOrchestrator {

    static interface Task {
        void run(ExecutorService executorService);
    }

    private static final class NamedThreadFactory implements ThreadFactory {

        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        private final Workflow workflow;
        private final Map<Workflow, List<Thread>> workflowThreads;

        NamedThreadFactory(Workflow workflow, Map<Workflow, List<Thread>> workflowThreads) {
            this.workflow = workflow;
            this.workflowThreads = workflowThreads;
            final SecurityManager s = System.getSecurityManager();
            this.group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            this.namePrefix = getWorkflowThreadPrefix(workflow);
        }

        @Override
        public Thread newThread(Runnable runnable) {
            final Thread thread = new Thread(group, runnable, namePrefix + threadNumber.getAndIncrement(), 0);
            thread.setDaemon(true);
            if (thread.getPriority() != Thread.NORM_PRIORITY) {
                thread.setPriority(Thread.NORM_PRIORITY);
            }
            
            workflowThreads.computeIfAbsent(workflow, k -> new CopyOnWriteArrayList<>()).add(thread);
            return thread;
        }
    }

    private static final String WORKFLOW_THREADS_NAME = "workflow-thread-";
    private static final int EXPECTED_THREADS_NUM = 3;

    private final int maxConcurrentWorkflows;
    private final ExecutorService orchestratorPool;
    
    private final Map<Workflow, Future<?>> activeWorkflows = new ConcurrentHashMap<>();
    private final Map<Workflow, List<Thread>> workflowThreads = new ConcurrentHashMap<>();

    private static final WorkflowOrchestrator instance = new WorkflowOrchestrator(1);

    private WorkflowOrchestrator(int maxConcurrentWorkflows) {
        this.orchestratorPool = new ThreadPoolExecutor(maxConcurrentWorkflows, maxConcurrentWorkflows, 1L,
                TimeUnit.SECONDS, new SynchronousQueue<>());
        this.maxConcurrentWorkflows = maxConcurrentWorkflows;
    }

    public static WorkflowOrchestrator instance() {
        return instance;
    }

    WorkflowExecutionStatus submit(@NonNull Workflow workflow, @NonNull Task task) {

        if (isRunning(workflow)) {
            log.warn("Orchestrator rejected start request. This specific workflow is already running.");
            return WorkflowExecutionStatus.REJECTED;
        }
        try {
            workflowThreads.remove(workflow);

            final Future<?> future = orchestratorPool.submit(() -> {
                try {
                    task.run(newExecutorService(workflow));
                } catch (Exception e) {
                    log.error("Workflow crashed", e);
                } finally {
                    activeWorkflows.remove(workflow);
                    workflowThreads.remove(workflow);
                }
            });
            activeWorkflows.put(workflow, future);

        } catch (RejectedExecutionException e) {
            log.warn("Orchestrator rejected start request. Maximum concurrent workflows ({}) reached.",
                    maxConcurrentWorkflows);
            return WorkflowExecutionStatus.REJECTED;
        }

        return WorkflowExecutionStatus.STARTED;
    }

    boolean isRunning(Workflow workflow) {
        final Future<?> task = activeWorkflows.get(workflow);
        return task != null && !task.isDone() && workflowThreadsRunning(workflow);
    }

    void stop(Workflow workflow) {
        final Future<?> task = activeWorkflows.get(workflow);
        if (task != null) {
            task.cancel(true);
            activeWorkflows.remove(workflow);
            workflowThreads.remove(workflow);
        }
    }

	private boolean workflowThreadsRunning(Workflow workflow) {
		final List<Thread> threads = workflowThreads.get(workflow);
		if (threads == null) {
			return false;
		}

		int aliveCount = 0;
		for (final Thread t : threads) {
			if (t.isAlive()) {
				aliveCount++;
			}
		}
		return aliveCount == EXPECTED_THREADS_NUM;
	}

    private ExecutorService newExecutorService(Workflow workflow) {
        return Executors.newFixedThreadPool(EXPECTED_THREADS_NUM, new NamedThreadFactory(workflow, workflowThreads));
    }

    private static String getWorkflowThreadPrefix(Workflow workflow) {
        return WORKFLOW_THREADS_NAME + workflow.hashCode() + "-";
    }
}