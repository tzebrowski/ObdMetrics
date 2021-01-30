package org.obd.metrics;

public interface Convertible<T> {
	
	Number getMinValue();
	T getValue();

	public default long valueToLong() {
		return  getValue() == null ? getMinValue().longValue() : ((Number) getValue()).longValue();
	}

	public default Double valueToDouble() {
		var multiplier = (int) Math.pow(10, 2);
		return getValue() == null ? getMinValue().doubleValue()
				: (double) ((long) ((Double.parseDouble(getValue().toString())) * multiplier)) / multiplier;
	}

	public default String valueAsString() {
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
}
