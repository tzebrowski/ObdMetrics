package org.obd.metrics.command.group;

import java.util.ArrayDeque;
import java.util.Deque;

import org.obd.metrics.command.Command;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class CommandGroup<T extends Command> {

	@Getter
	protected Deque<T> commands = new ArrayDeque<T>();

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
