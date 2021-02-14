package org.obd.metrics.command.group;

import java.util.concurrent.LinkedBlockingDeque;

import org.obd.metrics.command.Command;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class CommandGroup<T extends Command> {

	@Getter
	protected LinkedBlockingDeque<T> commands = new LinkedBlockingDeque<T>();

	@SuppressWarnings("unchecked")
	protected CommandGroup<T> of(T... commands) {
		for (final T command : commands) {
			this.commands.add(command);
		}
		return this;
	}

	protected CommandGroup<T> of(CommandGroup<T> parent) {
		for (final T command : parent.commands) {
			commands.add(command);
		}
		return this;
	}
}
