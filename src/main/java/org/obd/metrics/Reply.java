package org.obd.metrics;

import org.obd.metrics.command.Command;
import org.obd.metrics.raw.Raw;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@EqualsAndHashCode(of = "command")
public class Reply<T extends Command> {

	@Getter
	protected final T command;

	@Getter
	protected final Raw raw;

	@Getter
	protected final long timestamp = System.currentTimeMillis();

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
