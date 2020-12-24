package org.openobd2.core;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.SubmissionPublisher;

import org.openobd2.core.command.Command;
import org.openobd2.core.command.CommandReply;
import org.openobd2.core.command.QuitCommand;
import org.openobd2.core.streams.Streams;

import lombok.AllArgsConstructor;
import lombok.Builder;
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

	private final ConvertersRegistry converterRegistry = new ConvertersRegistry();

	@Builder
	static CommandExecutor build(Streams streams, CommandsBuffer buffer,
			@Singular("subscribe") List<Subscriber<CommandReply<?>>> subscribe) {

		final CommandExecutor commandExecutor = new CommandExecutor(streams, buffer);

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

		try (final DeviceIO device = DeviceIO.builder().streams(streams).build()) {
			while (true) {
				Thread.sleep(100);
				while (!commandsBuffer.isEmpty()) {

					final Command command = commandsBuffer.get();

					if (command instanceof QuitCommand) {
						log.info("Stopping command executor thread. Finishing communication.");
						publisher.submit(CommandReply.builder().command(command).build());

						return "stopped";
					} else {
						String data = null;
						try {
							device.write(command);
							Thread.sleep(10);
							data = device.read();
						} catch (IOException e) {
							log.error("Failed to execute command: {}", command);
							continue;
						}

						if (data.contains(STOPPED)) {
							log.error("Communication with the device is stopped.");
						} else if (data.contains(NO_DATA)) {
							log.debug("No data recieved.");
						} else if (data.contains(UNABLE_TO_CONNECT)) {
							log.error("Unable to connnect do device.");
						} else {
						}

						publisher.submit(CommandReply.builder().command(command).raw(data)
								.value(convertRawToValue(command, data)).build());
					}
				}
			}
		}
	}

	private Object convertRawToValue(final Command command, final String data) {
		Object value = null;
		// 41 indicates the success
		if (data.startsWith("41")) {
			value = converterRegistry.findConverter(command).map(p -> p.convert(data)).orElse(null);
		}

		return value;
	}
}