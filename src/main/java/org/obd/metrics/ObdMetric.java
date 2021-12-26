package org.obd.metrics;

import org.obd.metrics.command.obd.ObdCommand;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class ObdMetric extends Reply<ObdCommand> {

	private static final String NO_DATA_MESSAGE = "No data";

	@Getter
	protected final Object value;

	public long valueToLong() {
		return getValue() == null ? getMinValue().longValue() : ((Number) getValue()).longValue();
	}

	public Double valueToDouble() {
		final int multiplier = (int) Math.pow(10, 2);
		return getValue() == null ? getMinValue().doubleValue()
		        : (double) ((long) ((Double.parseDouble(getValue().toString())) * multiplier)) / multiplier;
	}

	public String valueToString() {
		if (getValue() == null) {
			return NO_DATA_MESSAGE;
		} else {
			if (getValue() instanceof Double) {
				return valueToDouble().toString();
			} else {
				return getValue().toString();
			}
		}
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("ObdMetric [com=");
		builder.append(command);
		builder.append(", val=");
		builder.append(value);
		builder.append(", raw=");
		builder.append(raw);
		builder.append("]");
		return builder.toString();
	}

	private Number getMinValue() {
		if (null == command.getPid().getMin()) {
			return Long.valueOf(0);
		} else {
			return command.getPid().getMin();
		}
	}
}
