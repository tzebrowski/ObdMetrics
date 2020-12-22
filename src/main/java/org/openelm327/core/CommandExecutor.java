package org.openelm327.core;

import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.SubmissionPublisher;

import org.openelm327.core.command.Command;
import org.openelm327.core.command.CommandResult;
import org.openelm327.core.command.QuitCommand;
import org.openelm327.core.command.ResetCommand;
import org.openelm327.core.streams.Streams;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class CommandExecutor extends Thread {

	private static final String STOPPED = "STOPPED";
	private static final String UNABLE_TO_CONNECT = "UNABLE TO CONNECT";
	private static final String NO_DATA = "NO DATA";

	private final Streams streams;
	private final Commands commands;
	private final SubmissionPublisher<CommandResult> publisher = new SubmissionPublisher<CommandResult>();

	CommandExecutor(Streams streams, Commands commands) {
		this.commands = commands;
		this.streams = streams;
	}

	@Builder
	static CommandExecutor build(Streams streams, Commands commands, Subscriber<CommandResult> subscriber) {
		CommandExecutor commandExecutor = new CommandExecutor(streams, commands);
		commandExecutor.publisher.subscribe(subscriber);
		return commandExecutor;
	}

	@Override
	public void run() {

		log.info("Starting command executor thread..");

		try (final IOManager io = IOManager.builder().streams(streams).build()) {
			while (true) {
				Thread.sleep(100);
				while (!commands.isEmpty()) {

					final Command command = commands.get();

					if (command instanceof QuitCommand) {
						log.info("Stopping command executor thread. Finishing communication.");
						return;
					} else {

						io.write(command);
						Thread.sleep(50);
						final String data = io.read(command);
						if (data.contains(STOPPED)) {
							commands.add(new ResetCommand());
						} else if (data.contains(NO_DATA)) {

						} else if (data.contains(UNABLE_TO_CONNECT)) {
						
						}
						
						final CommandResult commandResult = CommandResult.builder().command(command).raw(data).build();
						publisher.submit(commandResult);
					}
				}
			}
		} catch (Exception e) {
			log.error("Something went wrong...", e);
		}
	}
}