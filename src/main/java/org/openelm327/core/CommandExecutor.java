package org.openelm327.core;

import java.io.IOException;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.SubmissionPublisher;

import org.openelm327.core.command.Command;
import org.openelm327.core.command.CommandReply;
import org.openelm327.core.command.QuitCommand;
import org.openelm327.core.command.Transformation;
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
	private final SubmissionPublisher<CommandReply> publisher = new SubmissionPublisher<CommandReply>();

	CommandExecutor(Streams streams, Commands commands) {
		this.commands = commands;
		this.streams = streams;
	}

	@Builder
	static CommandExecutor build(Streams streams, Commands commands, Subscriber<CommandReply> subscriber) {
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
						
						String data = null;
					
						try {

							io.write(command);
							Thread.sleep(20);
							data = io.read(command);
						} catch (IOException e) {
							log.error("Failed to execute command: {}", command);
							continue;
						}
						
						if (data.contains(STOPPED)) {
							log.error("Communication with the device is stopped.");

						} else if (data.contains(NO_DATA)) {

						} else if (data.contains(UNABLE_TO_CONNECT)) {
							log.error("Unable to connnect do device.");
						} else {

						}

						final CommandReply commandReply = buildCommandReply(command, data);
						publisher.submit(commandReply);
					}
				}
			}
		} catch (Exception e) {
			log.error("Something went wrong...", e);
		}
	}

	private CommandReply buildCommandReply(final Command command, final String data) {
		final Object value = transformRawData(command, data);
		return CommandReply.builder().command(command).raw(data).value(value).build();
	}

	private Object transformRawData(final Command command, final String data) {
		Object value= null;
		// 41 indicates the success
		if (data.startsWith("41")) {
			if (command instanceof Transformation) {
				value = ((Transformation<?>) command).transform(data);
			}
		}
		return value;
	}
}