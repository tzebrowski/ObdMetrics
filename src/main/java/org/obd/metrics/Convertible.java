package org.obd.metrics;

public interface Convertible<T> {

	T getValue();
	
	public default long valueToLong() {
		return ((Number)getValue()).longValue();
	}
	
	public default double valueToDouble() {
		var multiplier = (int) Math.pow(10, 2);
		return getValue() == null ? 0.0
				: (double) ((long) ((Double.parseDouble(getValue().toString())) * multiplier)) / multiplier;
	}

	public default String valueAsString() {
		if (getValue() == null) {
			return "";
		} else {
			if (getValue().toString().contains(".")) {
				return String.format("%.2f", getValue());
			} else {
				return String.format("%d", getValue());
			}
		}
	}
}
