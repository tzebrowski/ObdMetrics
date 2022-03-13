package org.obd.metrics.codec;

import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.raw.RawMessage;

public interface Codec<T> {

	T decode(PidDefinition pid, RawMessage raw);
}
