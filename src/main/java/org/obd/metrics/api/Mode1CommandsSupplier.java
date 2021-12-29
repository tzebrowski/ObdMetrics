package org.obd.metrics.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.obd.metrics.ObdMetric;
import org.obd.metrics.Reply;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.codec.batch.Batchable;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.obd.SupportedPidsCommand;
import org.obd.metrics.pid.PidRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class Mode1CommandsSupplier extends ReplyObserver<Reply<?>>
        implements Supplier<Optional<Collection<ObdCommand>>> {

	private final PidRegistry pidRegistry;
	private final boolean batchEnabled;
	private final Collection<ObdCommand> commands = new ArrayList<ObdCommand>();

	Mode1CommandsSupplier(PidRegistry pidRegistry, boolean batchEnabled, Query query) {
		super();
		this.pidRegistry = pidRegistry;
		this.batchEnabled = batchEnabled;
		buildCommandsList(query);
	}

	@Override
	public String[] observables() {
		return new String[] { SupportedPidsCommand.class.getName() };
	}

	@Override
	public Optional<Collection<ObdCommand>> get() {
		if (commands.isEmpty()) {
			return Optional.empty();
		} else {
			return Optional.of(commands);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onNext(Reply<?> reply) {

		final List<String> value = (List<String>) ((ObdMetric) reply).getValue();
		log.info("PID's supported by ECU: {}", value);
	}

	private void buildCommandsList(final Query query) {
		final List<ObdCommand> commands = query.getPids()
		        .stream()
		        .map(pid -> new ObdCommand(pidRegistry.findBy(pid)))
		        .collect(Collectors.toList());
		commands.sort((c1, c2) -> c2.getPid().compareTo(c1.getPid()));
		
		
		if (batchEnabled) {
			// collect first commands that support batch fetching
			List<ObdCommand> collect = commands
			        .stream()
			        .filter(p -> p.getPid().isBatchable())
			        .collect(Collectors.toList());
			this.commands
			        .addAll(Batchable.encode(collect));
			// add at the end commands that support batch fetching
			this.commands.addAll(commands.stream().filter(p -> !p.getPid().isBatchable())
			        .collect(Collectors.toList()));

		} else {
			this.commands.addAll(commands);
		}
		log.info("Build command list: {}", commands);
	}

}
