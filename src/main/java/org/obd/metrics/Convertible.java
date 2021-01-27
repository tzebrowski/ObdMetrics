package org.obd.metrics;

public interface Convertible<T> {

	T getValue();

	public default long valueToLong() {
		return ((Number) getValue()).longValue();
	}

	public default Double valueToDouble() {
		var multiplier = (int) Math.pow(10, 2);
		return getValue() == null ? 0.0
				: (double) ((long) ((Double.parseDouble(getValue().toString())) * multiplier)) / multiplier;
	}

	public default String valueAsString() {
		if (getValue() == null) {
			return "";
		} else {
			if (getValue() instanceof Double) {
				return valueToDouble().toString();
			} else {
				return getValue().toString();
			}
		}
	}
}
