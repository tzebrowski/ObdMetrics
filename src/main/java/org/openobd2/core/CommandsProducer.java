package org.openobd2.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.openobd2.core.command.CommandReply;
import org.openobd2.core.command.at.EchoCommand;
import org.openobd2.core.command.at.HeadersCommand;
import org.openobd2.core.command.at.LineFeedCommand;
import org.openobd2.core.command.at.ResetCommand;
import org.openobd2.core.command.at.SelectProtocolCommand;
import org.openobd2.core.command.obd.mode1.CustomCommand;
import org.openobd2.core.command.obd.mode1.SupportedPidsCommand;
import org.openobd2.core.command.process.QuitCommand;
import org.openobd2.core.pid.PidDefinition;
import org.openobd2.core.pid.PidDefinitionRegistry;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
final class CommandsProducer extends CommandReplySubscriber implements Callable<String> {

	private final CommandsBuffer buffer;

	private final ProducerPolicy policy;

	@NonNull
	private final PidDefinitionRegistry pidDefinitionRegistry;

	@Default
	final Set<CustomCommand> cycleCommands = new HashSet();

	@Default
	private volatile boolean quit = false;

	@SuppressWarnings("unchecked")
	@Override
	public void onNext(CommandReply<?> reply) {
		subscription.request(1);

		if (reply.getCommand() instanceof SupportedPidsCommand) {
			final List<String> value = (List<String>) reply.getValue();
			if (value != null) {
				cycleCommands.addAll(value.stream().map(pid -> {
					final CustomCommand customCommand = new CustomCommand(pid);
					final PidDefinition pidDefinition = pidDefinitionRegistry.findBy(customCommand.getMode(), pid);
					customCommand.setPidDefinition(pidDefinition);
					
					return customCommand;
				}).filter(p -> true).collect(Collectors.toList()));
			}
		} else if (reply.getCommand() instanceof QuitCommand) {
			quit = true;
		}
	}

	@Override
	public String call() throws Exception {
		log.info("Staring publishing thread....");

		// init communication
		buffer.add(new ResetCommand());// reset
		buffer.add(new LineFeedCommand(0)); // line feed off
		buffer.add(new EchoCommand(0));// echo off

		buffer.add(new HeadersCommand(0));// headers off
		buffer.add(new SelectProtocolCommand(0)); // protocol default

		// query for supported pids
		buffer.add(new SupportedPidsCommand("00"));
		buffer.add(new SupportedPidsCommand("20"));
		buffer.add(new SupportedPidsCommand("40"));

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
