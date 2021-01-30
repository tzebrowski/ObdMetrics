package org.obd.metrics;

import java.text.NumberFormat;
import java.text.ParseException;

import org.obd.metrics.command.Command;
import org.obd.metrics.command.obd.ObdCommand;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
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
	public Number getMinValue() {
		if (command != null && command instanceof ObdCommand) {
			final ObdCommand obdCommand = (ObdCommand) command;
			try {
				return NumberFormat.getInstance().parse(obdCommand.getPid().getMin());
			} catch (ParseException e) {}
		}
		return Long.valueOf(0);
	}

	@Override
	public String toString() {
		var builder = new StringBuilder();
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
