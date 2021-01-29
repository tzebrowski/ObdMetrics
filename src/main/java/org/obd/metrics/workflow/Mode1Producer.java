package org.obd.metrics.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.obd.metrics.CommandsBuffer;
import org.obd.metrics.Metric;
import org.obd.metrics.ProducerPolicy;
import org.obd.metrics.codec.batch.Batchable;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.obd.SupportedPidsCommand;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidRegistry;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class Mode1Producer extends Producer implements Batchable {

	private final Collection<ObdCommand> supportedPids = new HashSet<ObdCommand>();
	private final PidRegistry pidRegistry;
	private final boolean batchEnabled;
	private final Set<String> filter;

	Mode1Producer(@NonNull CommandsBuffer buffer, @NonNull ProducerPolicy policy, PidRegistry pidRegistry,
			Set<String> filter,boolean batchEnabled) {
		super(buffer, policy);
		this.cycleCommands = new HashSet<ObdCommand>();
		this.filter = filter;
		this.pidRegistry = pidRegistry;
		this.batchEnabled = batchEnabled;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onNext(Metric<?> metric) {
		log.trace("Recieved OBD metric: {}", metric);
		super.onNext(metric);
		if (metric.getCommand() instanceof SupportedPidsCommand) {
			try {

				final List<String> value = (List<String>) metric.getValue();
				log.info("Supported pids command reply : {}", value);

				if (value != null) {
					final List<ObdCommand> commands = value.stream()
							.filter(p -> filter.isEmpty() ? true : filter.contains(p.toLowerCase())).map(pid -> {
								return toObdCommand(pid);
							}).filter(p -> p != null).collect(Collectors.toList());

					if (batchEnabled) {
						supportedPids.addAll(commands);
						cycleCommands.clear();
						cycleCommands.addAll(encode(new ArrayList<>(supportedPids)));
					} else {
						cycleCommands.addAll(commands);
					}

					log.info("Built list of supported PIDs : {}", cycleCommands);
				}
			} catch (Throwable e) {
				log.error("Failed to read supported pids", e);
			}
		}
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
