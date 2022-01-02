package org.obd.metrics.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.obd.metrics.DeviceProperties;
import org.obd.metrics.Lifecycle;
import org.obd.metrics.command.obd.ObdCommand;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
abstract class CommandsSuplier implements Supplier<Optional<Collection<ObdCommand>>>, Lifecycle {

	private final Query query;
	private Collection<ObdCommand> commands = Arrays.asList();

	abstract List<ObdCommand> map(Query query);

	@Override
	public void onRunning(DeviceProperties properties) {
		log.debug("Received INIT_COMPLETED event. Building cycle commands list.");
		commands = map(query);
	}

	@Override
	public Optional<Collection<ObdCommand>> get() {
		return Optional.of(commands);
	}
}