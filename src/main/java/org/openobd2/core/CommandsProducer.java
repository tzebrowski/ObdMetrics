package org.openobd2.core;

import java.util.concurrent.Callable;

import org.openobd2.core.command.EngineTempCommand;
import org.openobd2.core.command.ProtocolCloseCommand;
import org.openobd2.core.command.QuitCommand;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
@AllArgsConstructor
final class CommandsProducer implements Callable<String> {

	private final CommandsBuffer commands;
	private final int numOfCommands;

	@Override
	public String call() throws Exception {
		log.info("Staring publishing thread......");
		Thread.sleep(8000);

		for (int i = 0; i < numOfCommands; i++) {
			Thread.sleep(25);
			commands.add(new EngineTempCommand());
		}
		commands.add(new ProtocolCloseCommand()); // protocol close
		commands.add(new QuitCommand());// quite the CommandExecutor

		return null;
	}

}
