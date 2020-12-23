package org.openobd2.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.openobd2.core.command.CommandReply;
import org.openobd2.core.command.CustomCommand;
import org.openobd2.core.command.EchoCommand;
import org.openobd2.core.command.HeadersCommand;
import org.openobd2.core.command.LineFeedCommand;
import org.openobd2.core.command.QuitCommand;
import org.openobd2.core.command.ResetCommand;
import org.openobd2.core.command.SelectProtocolCommand;
import org.openobd2.core.command.SupportedPidsCommand;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
final class CommandsProducer extends CommandReplySubscriber implements Callable<String> {

	private final CommandsBuffer buffer;

	@Default
	private final List<String> pids = new ArrayList<String>();

	@Default
	private volatile boolean quit = false;

	@Default
	private Object pidsAvailableCondition = new Object();

	@SuppressWarnings("unchecked")
	@Override
	public void onNext(CommandReply<?> reply) {
		subscription.request(1);

		if (reply.getCommand() instanceof SupportedPidsCommand) {
			final CommandReply<SupportedPidsCommand> supportedPids = (CommandReply<SupportedPidsCommand>) reply;
			pids.addAll((List<String>) supportedPids.getValue());

			synchronized (pidsAvailableCondition) {
				pidsAvailableCondition.notify();
			}
		} else if (reply.getCommand() instanceof QuitCommand) {
			quit = true;
		} else {

		}
	}

	@Override
	public String call() throws Exception {
		log.info("Staring publishing thread....");

		// init communication
		buffer.add(new ResetCommand());// reset
		buffer.add(new LineFeedCommand(0)); // line feed off
		buffer.add(new HeadersCommand(0));// headers off
		buffer.add(new EchoCommand(0));// echo off
		buffer.add(new SelectProtocolCommand(0)); // protocol default

		// query for supported pids
		final SupportedPidsCommand supportedPidsCommand = new SupportedPidsCommand("00");
		buffer.add(supportedPidsCommand);

		waitForPids();

		log.info("Recieved supported pids: {}. Producer is able to query ECU", pids);

		final List<CustomCommand> commands = pids.stream()
				.map(pid -> new CustomCommand(supportedPidsCommand.getMode() + pid)).filter(p -> true)
				.collect(Collectors.toList());

		log.info("Built command set: {} ", commands);

		while (!quit) {
			// pushing every second
			TimeUnit.MILLISECONDS.sleep(500);
			buffer.addAll(commands);
		}

		log.info("Recieved QUIT command. Ending the process.");
		return null;
	}

	private void waitForPids() throws InterruptedException {
		synchronized (pidsAvailableCondition) {
			while (pids.isEmpty()) {
				pidsAvailableCondition.wait();
			}
		}
	}

}
