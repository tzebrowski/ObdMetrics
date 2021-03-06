package org.obd.metrics;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import org.obd.metrics.api.ConditionalSleep;
import org.obd.metrics.api.Workflow;

public interface WorkflowFinalizer {

	public static final int DEFAULT_FINALIZE_TIME = 500;

	static void finalizeAfter(final Workflow workflow, long sleepTime, Supplier<Boolean> condition)
	        throws InterruptedException {
		final Callable<String> end = () -> {
			var conditionalSleep = ConditionalSleep
			        .builder()
			        .condition(condition)
			        .slice(10l)
			        .build();

			conditionalSleep.sleep(sleepTime);
			workflow.stop();
			return "end";
		};

		var newFixedThreadPool = Executors.newFixedThreadPool(1);
		newFixedThreadPool.invokeAll(Arrays.asList(end));
		newFixedThreadPool.shutdown();
	}

	static void finalizeAfter500ms(final Workflow workflow) throws InterruptedException {
		finalizeAfter(workflow, DEFAULT_FINALIZE_TIME, () -> false);
	}
}
