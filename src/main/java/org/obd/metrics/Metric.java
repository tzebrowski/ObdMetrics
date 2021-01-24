package org.obd.metrics;

import org.obd.metrics.command.Command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@AllArgsConstructor()
@EqualsAndHashCode(of = "command")
public final class Metric<T> {
	private final int multiplier = (int) Math.pow(10, 2);

	@Getter
	private final Command command;

	@Getter
	private final T value;

	@Getter
	private final String raw;

	@Getter
	private final long timestamp = System.currentTimeMillis();

	public double valueToDouble() {
		return value == null ? 0.0 : round();
	}

	public String valueAsString() {
		if (value == null) {
			return "";
		} else {
			if (value.toString().contains(".")) {
				return String.format("%.2f", value);
			} else {
				return String.format("%d", value);
			}
		}
	}

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

	private double round() {
		return (double) ((long) ((Double.parseDouble(this.value.toString())) * multiplier)) / multiplier;
	}
}
