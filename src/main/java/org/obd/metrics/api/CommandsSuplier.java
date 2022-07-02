package org.obd.metrics.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.obd.metrics.api.model.Query;
import org.obd.metrics.codec.batch.BatchCodec;
import org.obd.metrics.command.obd.BatchObdCommand;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinition.CommandType;
import org.obd.metrics.pid.PidDefinitionRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class CommandsSuplier implements Supplier<List<ObdCommand>> {

	private final PidDefinitionRegistry pidRegistry;
	private final boolean batchEnabled;
	private final List<ObdCommand> commands;

	public CommandsSuplier(PidDefinitionRegistry pidRegistry, boolean batchEnabled, Query query) {
		this.pidRegistry = pidRegistry;
		this.batchEnabled = batchEnabled;
		this.commands = build(query);
	}

	@Override
	public List<ObdCommand> get() {
		return commands;
	}

	private List<ObdCommand> build(Query query) {
		final List<ObdCommand> commands = query.getPids()
		        .stream()
		        .map(idToPid())
		        .filter(Objects::nonNull)
		        .sorted((c1, c2) -> c2.getPid().compareTo(c1.getPid()))
		        .collect(Collectors.toList());
		final List<ObdCommand> result = new ArrayList<>();
		if (batchEnabled) {
			// collect first commands that support batch fetching
			final List<ObdCommand> obdCommands = commands
			        .stream()
			        .filter(p -> CommandType.OBD.equals(p.getPid().getCommandType()))
			        .filter(distinctByKey(c -> c.getPid().getPid()))
			        .collect(Collectors.toList());

			List<BatchObdCommand> encode = BatchCodec.instance(null, new ArrayList<>(obdCommands)).encode();

			result.addAll(encode);
			// add at the end commands that does not support batch fetching
			result.addAll(commands.stream().filter(p -> !CommandType.OBD.equals(p.getPid().getCommandType()))
			        .collect(Collectors.toList()));

		} else {
			result.addAll(commands);
		}
		log.info("Build target commands list: {}", result);
		return result;
	}

	private <T> Predicate<T> distinctByKey(
	        Function<? super T, ?> keyExtractor) {

		final Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

	private Function<? super Long, ? extends ObdCommand> idToPid() {
		return pid -> {
			final PidDefinition findBy = pidRegistry.findBy(pid);
			return findBy == null ? null : new ObdCommand(findBy);
		};
	}
}
