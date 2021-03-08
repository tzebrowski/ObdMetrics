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
	        @NonNull ReplyObserver observer, Lifecycle lifecycle, Integer desiredCommandFrequency) throws IOException {

		log.info("Creating an instance of {}. Command frequency: {}", Mode1Workflow.class.getSimpleName(),
		        desiredCommandFrequency);

		ParametersValidator.ensureCommandFreqIsValid(desiredCommandFrequency);

		return new Mode1Workflow(pidSpec, equationEngine, observer, lifecycle, desiredCommandFrequency);
	}

	@Builder(builderMethodName = "generic", builderClassName = "GenericBuilder", buildMethodName = "initialize")
	public static Workflow newGenericWorkflow(@NonNull PidSpec pidSpec, String equationEngine,
	        @NonNull ReplyObserver observer, Lifecycle lifecycle, Integer desiredCommandFrequency) throws IOException {

		log.info("Creating an instance of {}. Command frequency: {}", GenericWorkflow.class.getSimpleName(),
		        desiredCommandFrequency);

		ParametersValidator.ensureCommandFreqIsValid(desiredCommandFrequency);

		return new GenericWorkflow(pidSpec, equationEngine, observer, lifecycle, desiredCommandFrequency);
	}
}
