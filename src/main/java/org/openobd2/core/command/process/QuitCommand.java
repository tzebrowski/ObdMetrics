package org.openobd2.core.command.process;

import org.openobd2.core.command.Command;

public final class QuitCommand extends Command {
	public QuitCommand() {
		super("QUIT", "Quit command");
	}
}
