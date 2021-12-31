package org.obd.metrics.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.obd.metrics.codec.batch.Batchable;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.pid.PidDefinition.CommandType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class Mode1CommandsSupplier implements Supplier<Optional<Collection<ObdCommand>>> {

	private final PidDefinitionRegistry pidRegistry;
	private final boolean batchEnabled;
	private final Collection<ObdCommand> commands;

	Mode1CommandsSupplier(PidDefinitionRegistry pidRegistry, boolean batchEnabled, Query query) {
		super();
		this.pidRegistry = pidRegistry;
		this.batchEnabled = batchEnabled;
		commands = map(query);
	}

	@Override
	public Optional<Collection<ObdCommand>> get() {
		return Optional.of(commands);
	}

	private List<ObdCommand> map(final Query query) {
		final List<ObdCommand> commands = query.getPids()
		        .stream()
		        .map(idToPid())
		        .filter(Objects::nonNull)
		        .sorted((c1, c2) -> c2.getPid().compareTo(c1.getPid()))
		        .collect(Collectors.toList());
		final List<ObdCommand> result = new ArrayList<>();
		if (batchEnabled) {
			// collect first commands that support batch fetching
			result.addAll(Batchable.encode(commands
			        .stream()
			        .filter(p -> CommandType.OBD.equals(p.getPid().getCommandType()))
			        .collect(Collectors.toList())));
			// add at the end commands that does not support batch fetching
			result.addAll(commands.stream().filter(p -> !CommandType.OBD.equals(p.getPid().getCommandType()))
			        .collect(Collectors.toList()));

		} else {
			result.addAll(commands);
		}
		log.info("Build command list: {}", result);
		return result;
	}

	private Function<? super Long, ? extends ObdCommand> idToPid() {
		return pid -> {
			final PidDefinition findBy = pidRegistry.findBy(pid);
			return findBy == null ? null : new ObdCommand(findBy);
		};
	}
}
