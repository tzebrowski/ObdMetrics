package org.openelm327.core;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;

import org.openelm327.core.command.Command;
import org.openelm327.core.command.CommandResult;
import org.openelm327.core.command.QuitCommand;
import org.openelm327.core.command.ResetCommand;
import org.openelm327.core.streams.Streams;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
final class CommandExecutor extends Thread implements Publisher<CommandResult> {

	private static final String SEARCHING = "SEARCHING...";
	private static final String STOPPED = "STOPPED";
	private static final String UNABLE_TO_CONNECT = "UNABLE TO CONNECT";
	private static final String NO_DATA = "NO DATA";

	final Streams streams;
	final Commands commands;

	final List<Subscriber<? super CommandResult>> subscribers = new LinkedList<Subscriber<? super CommandResult>>();

	@Override
	public void subscribe(Subscriber<? super CommandResult> subscriber) {
		subscribers.add(subscriber);
	}

	@Override
	public void run() {
		IOManager io = null;
		try {
			io = IOManager.builder().streams(streams).build();

			while (true) {
				Thread.sleep(100);
				while (!commands.isEmpty()) {

					final Command atCommand = commands.get();

					if (atCommand instanceof QuitCommand) {
						io.close();
						log.info("Closing streams. Finishing communication.");
						return;
					} else {

						io.write(atCommand);
						Thread.sleep(50);
						final String data = io.read(atCommand);

						if (data.contains(STOPPED)) {
							Thread.sleep(1500);
							commands.add(new ResetCommand());
						} else if (data.contains(NO_DATA)) {
							Thread.sleep(1500);
						} else if (data.contains(UNABLE_TO_CONNECT)) {
							Thread.sleep(1500);
							commands.add(new ResetCommand());
							commands.add(atCommand);
						} else if (data.equals(SEARCHING)) {
							Thread.sleep(7000);
							commands.add(atCommand);
						} else {
							final CommandResult commandResult = CommandResult.builder().command(atCommand)
									.raw(data.replace(SEARCHING, "")).build();
							subscribers.forEach(p -> p.onNext(commandResult));
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("Something went wrong...", e);
		}finally {
			if (io != null) {
				io.close();
			}
		}
	}
}