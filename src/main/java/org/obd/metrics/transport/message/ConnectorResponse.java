package org.obd.metrics.transport.message;

import org.obd.metrics.codec.Decimals;
import org.obd.metrics.pid.CommandType;
import org.obd.metrics.pid.PidDefinition;

public interface ConnectorResponse {

	byte[] getBytes();

	int getLength();

	default void exctractDecimals(final PidDefinition pid, final DecimalReceiver decimalHandler) {
		for (int pos = pid.getSuccessCode().length(),
				j = 0; pos < getLength(); pos += 2, j++) {
			final int decimal = Decimals.twoBytesToDecimal(getBytes(), pos);
			decimalHandler.receive(j, decimal);
		}
	}

	default boolean isResponseCodeSuccess(PidDefinition pidDefinition) {
		if (CommandType.OBD.equals(pidDefinition.getCommandType())) {
			// success code = 0x40 + mode + pid
			return isReponseCodeSuccess(pidDefinition.getSuccessCodeBytes());
		} else {
			return true;
		}
	}
	
	default boolean isReponseCodeSuccess(final byte[] expectedAnswer) {
		return true;
	}
	
	default boolean isCacheable() {
		return false;
	}

	default long id() {
		return -1L;
	}

	default String getMessage() {
		return null;
	}

	default boolean isEmpty() {
		return false;
	}

	default boolean isError() {
		return false;
	}
}
