package org.obd.metrics.codec;

import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorMessage;

public interface Codec<T> {

	T decode(PidDefinition pid, ConnectorMessage raw);
}
