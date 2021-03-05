package org.obd.metrics.api;

import java.io.IOException;

import org.obd.metrics.Lifecycle;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.command.group.Mode1CommandGroup;
import org.obd.metrics.command.process.InitCompletedCommand;

final class Mode1Workflow extends AbstractWorkflow {

	Mode1Workflow(PidSpec pidSpec, String equationEngine, ReplyObserver observer,
	        Lifecycle lifecycle, Long commandFrequency) throws IOException {
		super(pidSpec, equationEngine, observer, lifecycle, commandFrequency);
	}

	@Override
	void init() {
		lifecycle.onConnecting();
		comandsBuffer.clear();
		pidSpec.getSequences().forEach(comandsBuffer::add);
		comandsBuffer.add(Mode1CommandGroup.SUPPORTED_PIDS);
		comandsBuffer.addLast(new InitCompletedCommand());
	}

	@Override
	Producer getProducer(WorkflowContext ctx) {
		return new Mode1Producer(comandsBuffer, producerPolicy, pids, ctx.filter, ctx.batchEnabled);
	}
}
