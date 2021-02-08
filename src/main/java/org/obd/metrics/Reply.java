package org.obd.metrics;

import org.obd.metrics.command.Command;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@EqualsAndHashCode(of = "command")
public class Reply {
	@Getter
	protected final Command command;

	@Getter
	protected final String raw;

	@Getter
	protected final long timestamp = System.currentTimeMillis();

	@Override
	public String toString() {
		var builder = new StringBuilder();
		builder.append("Reply [com=");
		builder.append(command);
		builder.append(", raw=");
		builder.append(raw);
		builder.append("]");
		return builder.toString();
	}
}
