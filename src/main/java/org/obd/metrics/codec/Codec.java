package org.obd.metrics.codec;

import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.raw.Raw;

public interface Codec<T> {

	T decode(PidDefinition pid, Raw raw);
}
