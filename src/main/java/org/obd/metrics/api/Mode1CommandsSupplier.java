package org.obd.metrics.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.obd.metrics.codec.batch.Batchable;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinition.CommandType;
import org.obd.metrics.pid.PidDefinitionRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class Mode1CommandsSupplier extends CommandsSuplier {

	private final PidDefinitionRegistry pidRegistry;
	private final boolean batchEnabled;

	Mode1CommandsSupplier(PidDefinitionRegistry pidRegistry, boolean batchEnabled, Query query) {
		super(query);
		this.pidRegistry = pidRegistry;
		this.batchEnabled = batchEnabled;
	}

	@Override
	List<ObdCommand> map(final Query query) {
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
