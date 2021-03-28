package org.obd.metrics;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.obd.metrics.api.Workflow;

public interface WorkflowFinalizer {

	public static final int DEFAULT_FINALIZE_TIME = 500;

	static void finalizeAfter(final Workflow workflow, long sleepTime) throws InterruptedException {
		final Callable<String> end = () -> {
			TimeUnit.MILLISECONDS.sleep(sleepTime);
			workflow.stop();
			return "end";
		};

		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(1);
		newFixedThreadPool.invokeAll(Arrays.asList(end));
		newFixedThreadPool.shutdown();
	}

	static void finalizeAfter500ms(final Workflow workflow) throws InterruptedException {
		finalizeAfter(workflow, DEFAULT_FINALIZE_TIME);
	}
}
