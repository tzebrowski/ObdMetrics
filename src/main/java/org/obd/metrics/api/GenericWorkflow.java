package org.obd.metrics.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.obd.metrics.DeviceProperties;
import org.obd.metrics.Lifecycle;
import org.obd.metrics.Lifecycle.LifeCycleSubscriber;
import org.obd.metrics.Reply;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.DelayCommand;
import org.obd.metrics.command.process.InitCompletedCommand;
import org.obd.metrics.pid.PidDefinition;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class GenericWorkflow extends AbstractWorkflow {

	final class CommandsSupplier implements Supplier<Optional<Collection<ObdCommand>>>, Lifecycle{

		private Collection<ObdCommand> commands = Arrays.asList();
		private final Query query;
		
		CommandsSupplier(Query query) {
			this.query = query;
		}
		
		@Override
		public void onRunning(DeviceProperties properties) {
			log.info("Received INIT_COMPLETED event. Building cycle commands list.");
			this.commands = map(query);
		}

		@Override
		public Optional<Collection<ObdCommand>> get() {
			return Optional.of(commands);
		}

		private Set<ObdCommand> map(Query query) {
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
			LifeCycleSubscriber lifecycle) throws IOException {
		super(pidSpec, equationEngine, observer, lifecycle);
	}

	@Override
	Supplier<Optional<Collection<ObdCommand>>> getCommandsSupplier(Adjustments adjustements, Query query) {
		final CommandsSupplier supplier = new CommandsSupplier(query);
		lifecycle.subscribe(supplier);
		return supplier;
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
