package org.obd.metrics.workflow;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.obd.metrics.MetricsObserver;
import org.obd.metrics.CommandsBuffer;
import org.obd.metrics.Metric;
import org.obd.metrics.ProducerPolicy;
import org.obd.metrics.codec.batch.Batchable;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.obd.SupportedPidsCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidRegistry;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
final class Mode1Producer extends MetricsObserver implements Callable<String>, Batchable {

	private final CommandsBuffer buffer;

	private final ProducerPolicy policy;

	@NonNull
	private final PidRegistry pidRegistry;

	@Default
	private final Collection<ObdCommand> cycleCommands = new HashSet();

	@Default
	private volatile boolean quit = false;

	private boolean batchEnabled;

	private final Set<String> filter;

	@SuppressWarnings("unchecked")
	@Override
	public void onNext(Metric<?> metric) {
		log.trace("Recieved OBD metric: {}", metric);

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
						cycleCommands.addAll(encode(commands));
					} else {
						cycleCommands.addAll(commands);
					}

					log.info("Built list of supported PIDs : {}", cycleCommands);
				}
			} catch (Throwable e) {
				log.error("Failed to read supported pids", e);
			}
		} else if (metric.getCommand() instanceof QuitCommand) {
			quit = true;
		}
	}

	@Override
	public String call() throws Exception {
		log.info("Staring publishing thread....");

		while (!quit) {

			TimeUnit.MILLISECONDS.sleep(policy.getDelayBeforeInsertingCommands());
			if (cycleCommands.isEmpty()) {
				TimeUnit.MILLISECONDS.sleep(policy.getEmptyBufferSleepTime());
			} else {
				buffer.addAll(cycleCommands);
			}
		}
		log.info("Recieved QUIT command. Ending the process.");
		return null;
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
