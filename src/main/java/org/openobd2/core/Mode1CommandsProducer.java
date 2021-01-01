package org.openobd2.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.openobd2.core.command.CommandReply;
import org.openobd2.core.command.CommandSet;
import org.openobd2.core.command.at.EchoCommand;
import org.openobd2.core.command.at.HeadersCommand;
import org.openobd2.core.command.at.LineFeedCommand;
import org.openobd2.core.command.at.ResetCommand;
import org.openobd2.core.command.at.SelectProtocolCommand;
import org.openobd2.core.command.obd.ObdCommand;
import org.openobd2.core.command.obd.SupportedPidsCommand;
import org.openobd2.core.command.process.QuitCommand;
import org.openobd2.core.pid.PidDefinition;
import org.openobd2.core.pid.PidRegistry;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
public final class Mode1CommandsProducer extends CommandReplySubscriber implements Callable<String> {

	private final CommandsBuffer buffer;

	private final ProducerPolicy policy;

	@NonNull
	private final PidRegistry pidDefinitionRegistry;

	@Default
	final Set<ObdCommand> cycleCommands = new HashSet();

	@Default
	private volatile boolean quit = false;

	@SuppressWarnings("unchecked")
	@Override
	public void onNext(CommandReply<?> reply) {
		subscription.request(1);

		if (reply.getCommand() instanceof SupportedPidsCommand) {
			try {
				final SupportedPidsCommand supportedPids = (SupportedPidsCommand) reply.getCommand();

				final List<String> value = (List<String>) reply.getValue();
				if (value != null) {
					cycleCommands.addAll(value.stream().map(pid -> {

						final PidDefinition pidDefinition = pidDefinitionRegistry
								.findBy(supportedPids.getPid().getMode(), pid);
						if (pidDefinition == null) {
							log.warn("No pid definition found for pid: {}", pid);
							return null;
						} else {
							return new ObdCommand(pidDefinition);
						}
					}).filter(p -> p != null).collect(Collectors.toList()));
				}
			} catch (Throwable e) {
				log.error("Failed to read supported pids", e);

			}
		} else if (reply.getCommand() instanceof QuitCommand) {
			quit = true;
		}
	}

	@Override
	public String call() throws Exception {
		log.info("Staring publishing thread....");

		buffer.add(new ResetCommand());// reset
		buffer.add(new LineFeedCommand(0)); // line feed off
		buffer.add(new HeadersCommand(0));// headers off
		buffer.add(new EchoCommand(0));// echo off
		buffer.add(new SelectProtocolCommand(0)); // protocol default

		// query for supported pids
		buffer.add(CommandSet.MODE1_SUPPORTED_PIDS);

		while (!quit) {
			TimeUnit.MILLISECONDS.sleep(policy.getFrequency());
			if (cycleCommands.isEmpty()) {
			} else {
				buffer.addAll(cycleCommands);
			}
		}

		log.info("Recieved QUIT command. Ending the process.");
		return null;
	}
}
