package org.obd.metrics.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.obd.metrics.ObdMetric;
import org.obd.metrics.Reply;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.codec.batch.Batchable;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.obd.SupportedPidsCommand;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
final class Mode1CommandsSupplier extends ReplyObserver<Reply<?>>
        implements Supplier<Optional<Collection<ObdCommand>>> {

	private final PidRegistry pidRegistry;
	private final boolean batchEnabled;
	private final Set<Long> filter;
	private final Collection<ObdCommand> batchTemp = new HashSet<ObdCommand>();
	private final Collection<ObdCommand> commands = new ArrayList<>();

	@Override
	public String[] observables() {
		return new String[] {
		        SupportedPidsCommand.class.getName(),
		};
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
		try {

			final List<String> value = (List<String>) ((ObdMetric) reply).getValue();
			log.info("PID's Supported by ECU: {}", value);

			if (value != null) {
				final List<ObdCommand> commands = value.stream().filter(this::contains).map(pid -> {
					return toObdCommand(pid);
				}).filter(p -> p != null).collect(Collectors.toList());

				if (batchEnabled) {
					batchTemp.addAll(commands);
					this.commands.clear();
					this.commands.addAll(Batchable.encode(new ArrayList<>(batchTemp)));
				} else {
					this.commands.addAll(commands);
				}

				log.info("Filtered cycle PID's : {}", commands);
			}
		} catch (Throwable e) {
			log.error("Failed to read supported pids", e);
		}
	}

	private boolean contains(String pid) {
		final PidDefinition pidDefinition = pidRegistry.findBy(pid);
		final boolean included = pidDefinition == null ? false
		        : (filter.isEmpty() ? true : filter.contains(pidDefinition.getId()));
		log.trace("Pid: {}  included:  {} ", pid, included);
		return included;
	}

	private ObdCommand toObdCommand(String pid) {
		final PidDefinition pidDefinition = pidRegistry.findBy(pid);
		if (pidDefinition == null) {
			log.warn("No pid definition found for pid: {}", pid);
			return null;
		} else {
			return new ObdCommand(pidDefinition);
		}
	}
}
