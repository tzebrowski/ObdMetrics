package org.obd.metrics.api;

import java.io.IOException;

import org.obd.metrics.ReplyObserver;
import org.obd.metrics.StatusObserver;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorkflowFactory {

	@Builder(builderMethodName = "mode1")
	public static Workflow newMode1Workflow(@NonNull EcuSpecific ecuSpecific, String equationEngine,
			@NonNull ReplyObserver observer, StatusObserver statusObserver, boolean enableStatistics,
			boolean enableGenerator, Double generatorIncrement) throws IOException {

		return new Mode1Workflow(ecuSpecific, equationEngine, observer, statusObserver, enableGenerator,
				generatorIncrement);
	}

	@Builder(builderMethodName = "generic", builderClassName = "GenericBuilder")
	public static Workflow newGenericWorkflow(@NonNull EcuSpecific ecuSpecific, String equationEngine,
			@NonNull ReplyObserver observer, StatusObserver statusObserver, boolean enableGenerator,
			Double generatorIncrement) throws IOException {

		return new GenericWorkflow(ecuSpecific, equationEngine, observer, statusObserver, enableGenerator,
				generatorIncrement);
	}

}
