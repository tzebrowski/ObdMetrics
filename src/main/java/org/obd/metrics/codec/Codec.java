package org.obd.metrics.codec;

import org.obd.metrics.model.RawMessage;
import org.obd.metrics.pid.PidDefinition;

public interface Codec<T> {

	T decode(PidDefinition pid, RawMessage raw);
}
