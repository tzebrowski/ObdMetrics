package org.obd.metrics.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.obd.metrics.Lifecycle;
import org.obd.metrics.Reply;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.DelayCommand;
import org.obd.metrics.command.process.InitCompletedCommand;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class GenericWorkflow extends AbstractWorkflow {

	final class CommandsSupplier implements Supplier<Optional<Collection<ObdCommand>>> {

		private final Set<ObdCommand> commands;

		CommandsSupplier(Query filter, PidRegistry pidRegistry) {
			this.commands = map(filter, pidRegistry);
		}

		private Set<ObdCommand> map(Query query, PidRegistry pidRegistry) {
			final Set<ObdCommand> cycleCommands = query.getPids().stream().map(pid -> {
				final PidDefinition pidDefinition = pidRegistry.findBy(pid);
				if (pidDefinition == null) {
					log.warn("No pid definition found for pid: {}", pid);
					return null;
				} else {
					return new ObdCommand(pidDefinition);
				}
			}).filter(p -> p != null).collect(Collectors.toSet());
			return cycleCommands;
		}

		@Override
		public Optional<Collection<ObdCommand>> get() {
			if (commands.isEmpty()) {
				return Optional.empty();
			} else {
				return Optional.of(commands);
			}
		}
	}

	GenericWorkflow(PidSpec pidSpec, String equationEngine, ReplyObserver<Reply<?>> observer,
	        Lifecycle lifecycle) throws IOException {
		super(pidSpec, equationEngine, observer, lifecycle);
	}

	@Override
	Supplier<Optional<Collection<ObdCommand>>> getCommandsSupplier(Adjustments adjustements, Query filter) {
		final CommandsSupplier commandsSupplier = new CommandsSupplier(filter, pidRegistry);
		log.info("Generic workflow selected commands: {}", commandsSupplier.get());
		return commandsSupplier;
	}

	@Override
	List<ReplyObserver<Reply<?>>> getObservers() {
		return Arrays.asList(commandProducer);
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
