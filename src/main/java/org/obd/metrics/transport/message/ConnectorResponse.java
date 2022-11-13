package org.obd.metrics.transport.message;

import org.obd.metrics.pid.CommandType;
import org.obd.metrics.pid.PidDefinition;

public interface ConnectorResponse {

	byte[] getBytes();

	int getLength();

	void exctractDecimals(PidDefinition pid, DecimalReceiver decimalHandler);

	default boolean isResponseCodeSuccess(PidDefinition pidDefinition) {
		if (CommandType.OBD.equals(pidDefinition.getCommandType())) {
			// success code = 0x40 + mode + pid
			return isAnswerCodeSuccess(pidDefinition.getSuccessAnswerCodeBytes());
		} else {
			return true;
		}
	}
	
	default boolean isCacheable() {
		return false;
	}

	default Long id() {
		return -1L;
	}

	default String getMessage() {
		return null;
	}

	default boolean isAnswerCodeSuccess(final byte[] expectedAnswer) {
		return true;
	}

	default boolean isEmpty() {
		return false;
	}

	default boolean isError() {
		return false;
	}
}
