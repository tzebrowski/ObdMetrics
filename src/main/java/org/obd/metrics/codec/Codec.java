package org.obd.metrics.codec;

import org.obd.metrics.pid.PidDefinition;

public interface Codec<T> {

	default T decode(PidDefinition pid, String raw) {
		return null;
	}
}
