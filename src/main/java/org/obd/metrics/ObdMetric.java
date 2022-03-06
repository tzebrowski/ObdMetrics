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
		if (value == null) {
			return getMinValue().longValue();
		} else {
			if (value instanceof Number) {
				return ((Number) getValue()).longValue();
			} else {
				return getMinValue().longValue();
			}
		}
	}

	public Double valueToDouble() {
		try {
			final int multiplier = (int) Math.pow(10, 2);
			return getValue() == null ? getMinValue().doubleValue()
			        : (double) ((long) ((Double.parseDouble(getValue().toString())) * multiplier)) / multiplier;
		} catch (NumberFormatException e) {
			return 0.0;
		}
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

	private Number getMinValue() {
		if (null == command.getPid().getMin()) {
			return Long.valueOf(0);
		} else {
			return command.getPid().getMin();
		}
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ObdMetric [pid=");
		builder.append(command.getPid().getPid());
		builder.append(", val=");
		builder.append(value);
		builder.append(", raw=");
		builder.append(raw);
		builder.append("]");
		return builder.toString();
	}	
}
