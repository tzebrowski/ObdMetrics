package org.obd.metrics.codec.formula;

import org.obd.metrics.pid.PidDefinition;

interface TypesConverter {

	static Number convert(PidDefinition pid, Object eval) {
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
