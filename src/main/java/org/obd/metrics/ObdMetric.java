package org.obd.metrics;

import java.text.NumberFormat;
import java.text.ParseException;

import org.obd.metrics.command.obd.ObdCommand;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class ObdMetric extends Reply {

	@Getter
	protected final Object value;

	@Override
	public ObdCommand getCommand() {
		return (ObdCommand) super.getCommand();
	}

	public long valueToLong() {
		return getValue() == null ? getMinValue().longValue() : ((Number) getValue()).longValue();
	}

	public Double valueToDouble() {
		var multiplier = (int) Math.pow(10, 2);
		return getValue() == null ? getMinValue().doubleValue()
				: (double) ((long) ((Double.parseDouble(getValue().toString())) * multiplier)) / multiplier;
	}

	public String valueToString() {
		if (getValue() == null) {
			return getMinValue().toString();
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
		var builder = new StringBuilder();
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
		try {
			return NumberFormat.getInstance().parse(getCommand().getPid().getMin());
		} catch (ParseException e) {
		}

		return Long.valueOf(0);
	}

}
