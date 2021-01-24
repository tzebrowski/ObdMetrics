package org.obd.metrics;

import org.obd.metrics.command.Command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@AllArgsConstructor()
@EqualsAndHashCode(of = "command")
public final class Metric<T> implements Convertible<T> {

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
		builder.append("Metric [com=");
		builder.append(command);
		builder.append(", val=");
		builder.append(value);
		builder.append(", raw=");
		builder.append(raw);
		builder.append("]");
		return builder.toString();
	}
}
