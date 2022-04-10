package org.obd.metrics.api;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public interface WorkflowFinalizer {

	public static final int DEFAULT_FINALIZE_TIME = 500;

	static void finalizeAfter(final Workflow workflow, long sleepTime, Supplier<Boolean> condition)
	        throws InterruptedException {
		final Callable<String> end = () -> {
			ConditionalSleep conditionalSleep = ConditionalSleep
			        .builder()
			        .condition(condition)
			        .slice(10l)
			        .build();

			conditionalSleep.sleep(sleepTime);
			workflow.stop();
			return "end";
		};

		ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(1);
		newFixedThreadPool.invokeAll(Arrays.asList(end));
		newFixedThreadPool.shutdown();
	}

	static void finalizeAfter(final Workflow workflow, long sleepTime) throws InterruptedException {
		finalizeAfter(workflow, sleepTime, () -> false);
	}

	static void finalizeAfter500ms(final Workflow workflow) throws InterruptedException {
		finalizeAfter(workflow, DEFAULT_FINALIZE_TIME, () -> false);
	}
}
