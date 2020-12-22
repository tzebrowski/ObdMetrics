package org.openelm327.core;

import java.util.concurrent.Callable;

import org.openelm327.core.command.EngineTempCommand;
import org.openelm327.core.command.ProtocolCloseCommand;
import org.openelm327.core.command.QuitCommand;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
@AllArgsConstructor
final class CommandsProducer implements Callable<String> {

	private final CommandsBuffer commands;

	@Override
	public String call() throws Exception {
		log.info("Staring publishing thread......");
		Thread.sleep(8000);
		
		for (int i = 0; i < 5; i++) {
			Thread.sleep(1000);
			log.info("Executing engine temp command");
			commands.add(new EngineTempCommand());
		}
		commands.add(new ProtocolCloseCommand()); // protocol close
		commands.add(new QuitCommand());// quite the CommandExecutor

		
		return null;
	}

}
