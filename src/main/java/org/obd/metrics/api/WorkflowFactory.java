package org.obd.metrics.api;

import java.io.IOException;

import org.obd.metrics.ReplyObserver;
import org.obd.metrics.Lifecycle;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * It creates different {@link Workflow} implementation.
 * 
 * @see EcuSpecific
 * @see Workflow
 * @see Lifecycle
 * @since 0.0.1
 * @author tomasz.zebrowski
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorkflowFactory {

	@Builder(builderMethodName = "mode1", buildMethodName = "initialize")
	public static Workflow newMode1Workflow(@NonNull EcuSpecific ecuSpecific, String equationEngine,
	        @NonNull ReplyObserver observer, Lifecycle lifecycle, GeneratorSpec generator, Long commandFrequency)
	        throws IOException {

		log.info("Creating an instance of Mode1 worklow. Command frequency: {}, generator: {} ", commandFrequency,
		        generator);

		return new Mode1Workflow(ecuSpecific, equationEngine, observer, lifecycle, commandFrequency, generator);
	}

	@Builder(builderMethodName = "generic", builderClassName = "GenericBuilder", buildMethodName = "initialize")
	public static Workflow newGenericWorkflow(@NonNull EcuSpecific ecuSpecific, String equationEngine,
	        @NonNull ReplyObserver observer, Lifecycle lifecycle, GeneratorSpec generator, Long commandFrequency)
	        throws IOException {

		log.info("Creating an instance of Generic worklow. Command frequency: {}, generator: {} ", commandFrequency,
		        generator);

		return new GenericWorkflow(ecuSpecific, equationEngine, observer, lifecycle, commandFrequency, generator);
	}

}
