package org.obd.metrics.transport.message;

import org.obd.metrics.pid.CommandType;
import org.obd.metrics.pid.PidDefinition;

public interface ConnectorResponse {

	byte byteAt(int index);

	int remaining();

	long capacity();

	default int toDecimal(final int pos) {
		final int RADIX = 16;

		int result = 0;
		int i = pos;
		int digit = Character.digit(byteAt(i++) & 0xFF, RADIX);
		result -= digit;

		digit = Character.digit(byteAt(i++) & 0xFF, RADIX);
		result *= RADIX;
		result -= digit;

		return -result;
	}

	default void exctractDecimals(final PidDefinition pid, final DecimalReceiver decimalHandler) {
		for (int pos = pid.getSuccessCode().length(), j = 0; pos < remaining(); pos += 2, j++) {
			decimalHandler.receive(j, toDecimal(pos));
		}
	}

	default int indexOf(final byte[] str, final int strCount, final int fromIndex) {

		final int valueCount = remaining();
		final byte first = str[0];
		final int max = (valueCount - strCount);
		for (int i = fromIndex; i <= max; i++) {
			if (byteAt(i) != first) {
				while (++i <= max && byteAt(i) != first) {
					;
				}
			}
			if (i <= max) {
				int j = i + 1;
				final int end = j + strCount - 1;
				for (int k = 1; j < end && byteAt(j) == str[k]; j++, k++) {
					;
				}
				if (j == end) {
					return i;
				}
			}
		}
		return -1;
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
	
	default boolean isTimeout() {
		return false;
	}
	
	default boolean isEmpty() {
		return false;
	}

	default boolean isError() {
		return false;
	}
}
