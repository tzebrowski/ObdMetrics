package org.obd.metrics.api;

import java.io.IOException;

import org.obd.metrics.ReplyObserver;
import org.obd.metrics.StatusObserver;
import org.obd.metrics.codec.CodecRegistry;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorkflowFactory {

	@Builder(builderMethodName = "mode1")
	public static Workflow newMode1Workflow(@NonNull EcuSpecific ecuSpecific, @NonNull String equationEngine,
			@NonNull ReplyObserver observer, StatusObserver statusObserver, boolean enableStatistics,
			boolean enableGenerator, Double generatorIncrement) throws IOException {

		final AbstractWorkflow workflow = new Mode1Workflow(ecuSpecific);
		workflow.replyObserver = observer;
		workflow.codec = CodecRegistry.builder().equationEngine(equationEngine).enableGenerator(enableGenerator)
				.generatorIncrement(generatorIncrement).build();
		workflow.status = statusObserver == null ? StatusObserver.DEFAULT : statusObserver;
		return workflow;
	}

	@Builder(builderMethodName = "generic", builderClassName = "GenericBuilder")
	public static Workflow newGenericWorkflow(@NonNull EcuSpecific ecuSpecific, @NonNull String equationEngine,
			@NonNull ReplyObserver observer, StatusObserver statusObserver, boolean enableGenerator,
			Double generatorIncrement) throws IOException {

		final AbstractWorkflow workflow = new GenericWorkflow(ecuSpecific);
		workflow.replyObserver = observer;
		workflow.codec = CodecRegistry.builder().equationEngine(equationEngine).enableGenerator(enableGenerator)
				.generatorIncrement(generatorIncrement).build();
		workflow.status = statusObserver == null ? StatusObserver.DEFAULT : statusObserver;
		return workflow;
	}
}
