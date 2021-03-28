package org.obd.metrics;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.obd.metrics.api.Workflow;

public interface WorkflowFinalizer {

	static void setup(final Workflow workflow, long sleepTime) throws InterruptedException {
		final Callable<String> end = () -> {
			Thread.sleep(sleepTime);
			workflow.stop();
			return "end";
		};

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(1);
		newFixedThreadPool.invokeAll(Arrays.asList(end));
		newFixedThreadPool.shutdown();
	}

	static void setup(final Workflow workflow) throws InterruptedException {
		setup(workflow, 500);
	}
}
