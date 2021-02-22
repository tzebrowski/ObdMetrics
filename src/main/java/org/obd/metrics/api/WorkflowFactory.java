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
 * It initiates different {@link Workflow} implementation.
 * 
 * @see EcuSpecific
 * @see Workflow
 * @since 0.0.1
 * @author tomasz.zebrowski
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorkflowFactory {

	@Builder(builderMethodName = "mode1", buildMethodName = "initialize")
	public static Workflow newMode1Workflow(@NonNull EcuSpecific ecuSpecific, String equationEngine,
	        @NonNull ReplyObserver observer, Lifecycle lifecycle, boolean enableGenerator,
	        Double generatorIncrement, Long commandFrequency) throws IOException {
		log.info("Creating an instance of Mode1 worklow. Command frequency: {}, generator enabled: {} ",
		        commandFrequency, enableGenerator);

		return new Mode1Workflow(ecuSpecific, equationEngine, observer, lifecycle, enableGenerator,
		        generatorIncrement, commandFrequency);
	}

	@Builder(builderMethodName = "generic", builderClassName = "GenericBuilder", buildMethodName = "initialize")
	public static Workflow newGenericWorkflow(@NonNull EcuSpecific ecuSpecific, String equationEngine,
	        @NonNull ReplyObserver observer, Lifecycle lifecycle, boolean enableGenerator,
	        Double generatorIncrement, Long commandFrequency) throws IOException {

		log.info("Creating an instance of Generic worklow. Command frequency: {}, generator enabled: {} ",
		        commandFrequency, enableGenerator);

		return new GenericWorkflow(ecuSpecific, equationEngine, observer, lifecycle, enableGenerator,
		        generatorIncrement, commandFrequency);
	}

}
