package org.obd.metrics.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.obd.metrics.Lifecycle.LifeCycleSubscriber;
import org.obd.metrics.Reply;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.command.group.Mode1CommandGroup;
import org.obd.metrics.command.process.DelayCommand;
import org.obd.metrics.command.process.InitCompletedCommand;

final class Mode1Workflow extends AbstractWorkflow {

	Mode1Workflow(PidSpec pidSpec, String equationEngine, ReplyObserver<Reply<?>> observer,
	        LifeCycleSubscriber lifecycle) throws IOException {
		super(pidSpec, equationEngine, observer, lifecycle);
	}

	@Override
	void init(Adjustments adjustments) {
		lifecycle.onConnecting();
		commandsBuffer.clear();
		pidSpec.getSequences().forEach(commandsBuffer::add);
		commandsBuffer.add(Mode1CommandGroup.SUPPORTED_PIDS);
		commandsBuffer.addLast(new DelayCommand(adjustments.getInitDelay()));
		commandsBuffer.addLast(new InitCompletedCommand());
	}

	@Override
	CommandsSuplier getCommandsSupplier(Adjustments adjustements, Query query) {
		final Mode1CommandsSupplier supplier = new Mode1CommandsSupplier(pidRegistry, adjustements.isBatchEnabled(),
		        query);
		lifecycle.subscribe(supplier);
		return supplier;
	}

	@Override
	List<ReplyObserver<Reply<?>>> getObservers() {
		return Arrays.asList(commandProducer);
	}
}
