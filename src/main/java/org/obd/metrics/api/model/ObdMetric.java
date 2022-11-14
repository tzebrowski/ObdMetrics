package org.obd.metrics.api.model;

import org.obd.metrics.command.obd.ObdCommand;

import lombok.Getter;
import lombok.experimental.SuperBuilder;


@SuperBuilder
public class ObdMetric extends Reply<ObdCommand> {

	private static final String NO_DATA_MESSAGE = "No data";
	private static final int multiplier = (int) Math.pow(10, 2);
	
	@Getter
	protected final Object value;

	public double valueToDouble() {
		return getValue() == null ? Double.NaN
		        : (double) ((long) ((Double.parseDouble(getValue().toString())) * multiplier)) / multiplier;
	}

	public String valueToString() {
		if (getValue() == null) {
			return NO_DATA_MESSAGE;
		} else {
			if (getValue() instanceof Double) {
				return String.valueOf(valueToDouble());
			} else {
				return getValue().toString();
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ObdMetric [pid=");
		builder.append(command.getPid().getPid());
		builder.append(", id=");
		builder.append(command.getPid().getId());
		builder.append(", val=");
		builder.append(value);
		builder.append(", raw=");
		builder.append(raw);
		builder.append("]");
		return builder.toString();
	}	
}
