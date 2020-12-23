package org.openobd2.core;

import java.util.List;
import java.util.concurrent.Callable;

import org.openobd2.core.command.CommandReply;
import org.openobd2.core.command.CustomCommand;
import org.openobd2.core.command.EchoCommand;
import org.openobd2.core.command.EngineTempCommand;
import org.openobd2.core.command.HeadersCommand;
import org.openobd2.core.command.LineFeedCommand;
import org.openobd2.core.command.QuitCommand;
import org.openobd2.core.command.SupportedPidsCommand;
import org.openobd2.core.command.ResetCommand;
import org.openobd2.core.command.SelectProtocolCommand;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
final class CommandsProducer extends CommandReplySubscriber implements Callable<String> {

	private final CommandsBuffer buffer;

	@Default
	private volatile CommandReply supportedPids = null;

	@Default
	private volatile boolean quit = false;

	@Override
	public void onNext(CommandReply reply) {
		subscription.request(1);
		if (reply.getCommand() instanceof SupportedPidsCommand) {
			supportedPids = reply;
		}
		if (reply.getCommand() instanceof QuitCommand) {
			quit = true;
		}
	}

	@Override
	public String call() throws Exception {
		log.info("Staring publishing thread....");

		// init communication
		initCommunication();

		while (supportedPids == null) {
			Thread.sleep(100);
		}

		@SuppressWarnings("unchecked")
		List<String> value = (List<String>) supportedPids.getValue();
		log.info("Recieved supported pids: {}. Producer is able to query ECU", value);

		while (!quit) {
			// pushing every second
			Thread.sleep(1000);

			value.forEach(pid -> {
				log.info("Pushing custom pid command: {}",pid);
				buffer.add(new CustomCommand(pid));
			});
			buffer.add(new EngineTempCommand());
		}

		log.info("Recieved QUIT command. Ending the process.");
		return null;
	}

	private void initCommunication() {
		buffer.add(new ResetCommand());// reset
		buffer.add(new LineFeedCommand(0)); // line feed off
		buffer.add(new HeadersCommand(0));// headers off
		buffer.add(new EchoCommand(0));// echo off
		buffer.add(new SelectProtocolCommand(0)); // protocol default

		// query for supported pids
		buffer.add(new SupportedPidsCommand("00"));

	}

}
