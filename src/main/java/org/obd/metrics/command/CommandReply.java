package org.obd.metrics.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(of = "command")
@Builder
@AllArgsConstructor()
public final class CommandReply<T> {

	@Getter
	private final Command command;

	@Getter
	private final T value;

	@Getter
	private final String raw;

	@Getter
	private final long timestamp = System.currentTimeMillis();

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Reply [com=");
		builder.append(command);
		builder.append(", val=");
		builder.append(value);
		builder.append(", raw=");
		builder.append(raw);
		builder.append("]");
		
		return builder.toString();
	}
}
