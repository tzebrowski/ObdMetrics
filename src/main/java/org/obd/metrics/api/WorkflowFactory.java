package org.obd.metrics.api;

import java.io.IOException;

import org.obd.metrics.Lifecycle;
import org.obd.metrics.ReplyObserver;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * It creates different {@link Workflow} implementation.
 * 
 * @see PidSpec
 * @see Workflow
 * @see Lifecycle
 * @since 0.0.1
 * @author tomasz.zebrowski
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorkflowFactory {

	@Builder(builderMethodName = "mode1", buildMethodName = "initialize")
	public static Workflow newMode1Workflow(@NonNull PidSpec pidSpec, String equationEngine,
	        @NonNull ReplyObserver observer, Lifecycle lifecycle, Long commandFrequency) throws IOException {

		log.info("Creating an instance of Mode1 worklow. Command frequency: {}", commandFrequency);

		return new Mode1Workflow(pidSpec, equationEngine, observer, lifecycle, commandFrequency);
	}

	@Builder(builderMethodName = "generic", builderClassName = "GenericBuilder", buildMethodName = "initialize")
	public static Workflow newGenericWorkflow(@NonNull PidSpec pidSpec, String equationEngine,
	        @NonNull ReplyObserver observer, Lifecycle lifecycle, Long commandFrequency) throws IOException {

		log.info("Creating an instance of Generic worklow. Command frequency: {}", commandFrequency);
		if (commandFrequency != null && commandFrequency < 2) {
			throw new IllegalArgumentException("Command frequency must be greater than 2");
		}
		return new GenericWorkflow(pidSpec, equationEngine, observer, lifecycle, commandFrequency);
	}
}
