package org.obd.metrics.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.obd.metrics.CommandsBuffer;
import org.obd.metrics.ObdMetric;
import org.obd.metrics.ProducerPolicy;
import org.obd.metrics.Reply;
import org.obd.metrics.codec.batch.Batchable;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.obd.SupportedPidsCommand;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidRegistry;
import org.obd.metrics.statistics.StatisticsRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class Mode1Producer extends Producer {

	private final Collection<ObdCommand> supportedPids = new HashSet<ObdCommand>();
	private final PidRegistry pidRegistry;
	private final boolean batchEnabled;
	private final Set<Long> filter;

	Mode1Producer(StatisticsRegistry statisticsRegistry, CommandsBuffer buffer, ProducerPolicy policy,
	        PidRegistry pidRegistry,
	        Set<Long> filter, boolean batchEnabled) {
		super(statisticsRegistry, buffer, policy, new ArrayList<>());
		this.filter = filter;
		this.pidRegistry = pidRegistry;
		this.batchEnabled = batchEnabled;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onNext(Reply<?> reply) {
		log.trace("Recieved command reply: {}", reply);
		super.onNext(reply);
		if (reply.getCommand() instanceof SupportedPidsCommand) {
			try {

				final List<String> value = (List<String>) ((ObdMetric) reply).getValue();
				log.info("Supported by ECU PID's: {}", value);

				if (value != null) {
					final List<ObdCommand> commands = value.stream().filter(this::contains).map(pid -> {
						return toObdCommand(pid);
					}).filter(p -> p != null).collect(Collectors.toList());

					if (batchEnabled) {
						supportedPids.addAll(commands);
						cycleCommands.clear();
						cycleCommands.addAll(Batchable.encode(new ArrayList<>(supportedPids)));
					} else {
						cycleCommands.addAll(commands);
					}

					log.info("Filtered cycle PID's : {}", cycleCommands);
				}
			} catch (Throwable e) {
				log.error("Failed to read supported pids", e);
			}
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
