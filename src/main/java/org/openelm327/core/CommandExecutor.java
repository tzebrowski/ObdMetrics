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
@Builder
final class CommandExecutor extends Thread  {

	private static final String SEARCHING = "SEARCHING...";
	private static final String STOPPED = "STOPPED";
	private static final String UNABLE_TO_CONNECT = "UNABLE TO CONNECT";
	private static final String NO_DATA = "NO DATA";

	final Streams streams;
	final Commands commands;
	final SubmissionPublisher<CommandResult> publisher = new SubmissionPublisher<CommandResult>();
	
	public void subscribe(Subscriber<? super CommandResult> subscriber) {
		publisher.subscribe(subscriber);
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
							Thread.sleep(1500);

						} else if (data.equals(SEARCHING)) {
							log.info("searching...." + command);
							Thread.sleep(1000);
							commands.add(command);
						}
						final CommandResult commandResult = CommandResult.builder().command(command)
								.raw(data.replace(SEARCHING, "")).build();
						publisher.submit(commandResult);
					}
				}
			}
		} catch (Exception e) {
			log.error("Something went wrong...", e);
		}
	}
}