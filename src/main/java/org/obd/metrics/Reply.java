package org.obd.metrics;

import org.obd.metrics.command.Command;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@EqualsAndHashCode(of = "command")
public class Reply<T extends Command> {

	@Getter
	protected final T command;

	@Getter
	protected final String raw;

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Reply [command=");
		builder.append(command);
		builder.append(", raw=");
		builder.append(raw);
		builder.append("]");
		return builder.toString();
	}
}
