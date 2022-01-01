package org.obd.metrics.api;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import org.obd.metrics.Lifecycle;
import org.obd.metrics.Reply;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.DelayCommand;
import org.obd.metrics.command.process.InitCompletedCommand;
import org.obd.metrics.pid.PidDefinition;

import lombok.extern.slf4j.Slf4j;


@Slf4j
final class GenericWorkflow extends AbstractWorkflow {

	final class GenericSupplier extends CommandsSuplier {
		GenericSupplier(Query query) {
			super(query);
		}

		@Override
		Set<ObdCommand> map(Query query) {
			return query.getPids().stream().map(this::map).filter(p -> p != null).collect(Collectors.toSet());
		}

		private ObdCommand map(long pid) {
			final PidDefinition pidDefinition = pidRegistry.findBy(pid);
			if (pidDefinition == null) {
				log.warn("No pid definition found for pid: {}", pid);
				return null;
			} else {
				return new ObdCommand(pidDefinition);
			}
		}
	}

	GenericWorkflow(PidSpec pidSpec, String equationEngine, ReplyObserver<Reply<?>> observer,
			Lifecycle lifecycle) throws IOException {
		super(pidSpec, equationEngine, observer, lifecycle);
	}

	@Override
	CommandsSuplier getCommandsSupplier(Adjustments adjustements, Query query) {
		return new GenericSupplier(query);
	}

	@Override
	void init(Adjustments adjustments) {
		lifecycle.onConnecting();
		commandsBuffer.clear();
		pidSpec.getSequences().forEach(commandsBuffer::add);
		commandsBuffer.addLast(new DelayCommand(adjustments.getInitDelay()));
		commandsBuffer.addLast(new InitCompletedCommand());
	}
}
