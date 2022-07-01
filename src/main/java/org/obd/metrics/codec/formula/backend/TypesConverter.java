package org.obd.metrics.codec.formula.backend;

import org.obd.metrics.pid.PidDefinition;

interface TypesConverter {

	static Number convert(final PidDefinition pid, final Object eval) {
		final Number value = Number.class.cast(eval);
		if (pid.getType() == null) {
			return value.doubleValue();
		} else {
			switch (pid.getType()) {
			case INT:
				return value.intValue();
			case DOUBLE:
				return value.doubleValue();
			case SHORT:
				return value.shortValue();
			default:
				return value;
			}
		}
	}
}
