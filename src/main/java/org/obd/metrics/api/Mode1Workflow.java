package org.obd.metrics.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.obd.metrics.Lifecycle;
import org.obd.metrics.Reply;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.command.group.Mode1CommandGroup;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.InitCompletedCommand;

final class Mode1Workflow extends AbstractWorkflow {

	private Mode1CommandsSupplier commandsSupplier;

	Mode1Workflow(PidSpec pidSpec, String equationEngine, ReplyObserver<Reply<?>> observer,
	        Lifecycle lifecycle) throws IOException {
		super(pidSpec, equationEngine, observer, lifecycle);
	}

	@Override
	void init() {
		lifecycle.onConnecting();
		commandsBuffer.clear();
		pidSpec.getSequences().forEach(commandsBuffer::add);
		commandsBuffer.add(Mode1CommandGroup.SUPPORTED_PIDS);
		commandsBuffer.addLast(new InitCompletedCommand());
	}

	@Override
	Supplier<Optional<Collection<ObdCommand>>> getCommandsSupplier(Adjustments adjustements, Query query) {
		commandsSupplier = new Mode1CommandsSupplier(pidRegistry,
		        adjustements.isBatchEnabled(), query.getPids());
		return commandsSupplier;
	}

	@Override
	List<ReplyObserver<Reply<?>>> getObservers() {
		return Arrays.asList(commandProducer, commandsSupplier);
	}
}
