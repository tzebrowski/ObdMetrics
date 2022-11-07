package org.obd.metrics.codec;

import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;

public interface Codec<T> {

	T decode(PidDefinition pid, ConnectorResponse connectorResponse);

	default T decode(ConnectorResponse connectorResponse) {
		return decode(null, connectorResponse);
	}
}
