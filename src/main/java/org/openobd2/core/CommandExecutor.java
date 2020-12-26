package org.openobd2.core;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.SubmissionPublisher;

import org.openobd2.core.command.Command;
import org.openobd2.core.command.CommandReply;
import org.openobd2.core.command.at.ProtocolCloseCommand;
import org.openobd2.core.command.process.QuitCommand;
import org.openobd2.core.converter.ConvertersRegistry;
import org.openobd2.core.streams.Streams;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
final class CommandExecutor implements Callable<String> {

	private static final String STOPPED = "STOPPED";
	private static final String UNABLE_TO_CONNECT = "UNABLE TO CONNECT";
	private static final String NO_DATA = "NO DATA";

	private final Streams streams;
	private final CommandsBuffer commandsBuffer;
	private final SubmissionPublisher<CommandReply<?>> publisher = new SubmissionPublisher<CommandReply<?>>();
	private final ExecutorPolicy executorPolicy;
	private final ConvertersRegistry converterRegistry;

	@Builder
	static CommandExecutor build(
			@NonNull Streams streams,
			@NonNull CommandsBuffer buffer,
			@Singular("subscribe") List<Subscriber<CommandReply<?>>> subscribe,
			@NonNull  ExecutorPolicy policy,
			@NonNull ConvertersRegistry converterRegistry) {

		final CommandExecutor commandExecutor = new CommandExecutor(streams, buffer, policy,converterRegistry);

		if (null == subscribe || subscribe.isEmpty()) {
			log.info("no subscriber specified");
		} else {
			subscribe.forEach(s -> commandExecutor.publisher.subscribe(s));
		}

		return commandExecutor;
	}

	@Override
	public String call() throws Exception {

		log.info("Starting command executor thread..");

		try (final Streams streams = this.streams.open();) {
			while (true) {
				Thread.sleep(executorPolicy.getFrequency());
				while (!commandsBuffer.isEmpty()) {

					final Command command = commandsBuffer.get();

					if (command instanceof QuitCommand) {
						log.info("Stopping command executor thread. Finishing communication.");

						try {
							streams.write(new ProtocolCloseCommand());
						} catch (IOException e) {
							log.error("Failed to execute command: {}", command);
							continue;
						}
						publisher.submit(CommandReply.builder().command(command).build());

						return "stopped";
					} else {
						final String data = executeCommand(streams, command);
						if (null == data) {
							continue;
						} else if (data.contains(STOPPED)) {
							log.error("Communication with the device is stopped.");
						} else if (data.contains(NO_DATA)) {
							log.debug("No data recieved.");
						} else if (data.contains(UNABLE_TO_CONNECT)) {
							log.error("Unable to connnect do device.");
						} else {
						}

						try {
							publisher.submit(CommandReply
											.builder()
											.command(command)
											.raw(data)
											.value(converterRegistry.findConverter(command).map(p -> p.convert(data)).orElse(null))
											.build());
						} catch (Throwable e) {
							log.error("Failed to submit command reply", e);
						}
					}
				}
			}
		}
	}

	String executeCommand(Streams streams, Command command) {
		String data = null;
		try {
			streams.write(command);
			data = streams.read();
		} catch (IOException e) {
			log.error("Failed to execute command: {}", command);
		}
		return data;
	}
}