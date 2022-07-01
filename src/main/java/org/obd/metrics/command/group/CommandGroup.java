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
	protected Deque<T> commands = new ArrayDeque<>();

	@SuppressWarnings("unchecked")
	protected CommandGroup<T> of(final T... commands) {
		for (final T command : commands) {
			this.commands.add(command);
		}
		return this;
	}
}
