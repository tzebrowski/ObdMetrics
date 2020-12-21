package org.openelm327.core;

import java.io.InputStreamReader;
import java.io.OutputStream;

import org.openelm327.core.command.ATCommand;
import org.openelm327.core.command.QuitCommand;
import org.openelm327.core.command.ResetCommand;
import org.openelm327.core.streams.Streams;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class CommandExecutor extends Thread {

	private static final String SEARCHING = "SEARCHING...";
	private static final String STOPPED = "STOPPED";
	private static final String UNABLE_TO_CONNECT = "UNABLE TO CONNECT";
	private static final String NO_DATA = "NO DATA";
	final Streams streams;
	final Commands commands;

	CommandExecutor(Streams streams, Commands commandQueue) {
		this.streams = streams;
		this.commands = commandQueue;
	}

	@Override
	public void run() {
		try (final OutputStream os = streams.getOutputStream();
				final InputStreamReader in = new InputStreamReader(streams.getInputStream())) {

			final IOManager io = IOManager.builder().in(in).os(os).build();

			while (true) {
				Thread.sleep(100);
				while (!commands.isEmpty()) {

					final ATCommand command = commands.get();

					if (command instanceof QuitCommand) {
						io.closeQuitly();
						log.info("Closing streams. Finishing communication.");
						return;
					} else {

						io.write(command);
						Thread.sleep(50);
						final String data = io.read(command);

						if (data.contains(STOPPED)) {
							Thread.sleep(1500);
							commands.add(new ResetCommand());
						} else if (data.contains(NO_DATA)) {
							Thread.sleep(1500);
						} else if (data.contains(UNABLE_TO_CONNECT)) {
							Thread.sleep(1500);
							commands.add(new ResetCommand());
							commands.add(command);
						} else if (data.contains(SEARCHING)) {
							Thread.sleep(7000);
							commands.add(command);
						} else {

						}
					}
				}
			}
		} catch (Exception e) {
			log.error("Something failed...", e);
		}
	}
}