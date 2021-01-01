package org.openobd2.core.command.group;

import java.util.concurrent.LinkedBlockingDeque;

import org.openobd2.core.command.Command;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class CommandGroup<T extends Command> {

	@Getter
	protected LinkedBlockingDeque<T> commands = new LinkedBlockingDeque<T>();

	@SuppressWarnings("unchecked")
	protected static <T extends Command> CommandGroup<T> of(T... commands) {
		final CommandGroup<T> cs = new CommandGroup<T>();
		for (final T command : commands) {
			cs.commands.add(command);
		}
		return cs;
	}
}
