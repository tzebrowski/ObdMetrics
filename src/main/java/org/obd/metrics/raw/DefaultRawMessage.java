package org.obd.metrics.raw;

import java.nio.charset.Charset;
import java.util.Arrays;

import org.obd.metrics.codec.AnswerCodeCodec;
import org.obd.metrics.codec.Decimals;
import org.obd.metrics.pid.PidDefinition;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(of = "message")
final class DefaultRawMessage implements RawMessage {
	private static final Charset CHARSET = Charset.forName("ISO-8859-1");

	@Getter
	private boolean isError;

	@Getter
	private boolean isEmpty;

	private String message;

	@Getter
	private final byte[] bytes;

	@Override
	public String getMessage() {
		if (message == null && bytes != null) {
			message = new String(bytes, CHARSET);
		}
		return message;
	}

	DefaultRawMessage(byte bytes[]) {
		this.isEmpty = isEmpty(bytes);
		this.message = null;
		this.bytes = bytes;
		this.isError = isError(bytes);

	}

	@Override
	public void toDecimals(PidDefinition pid, DecimalHandler decimalHandler) {
		for (int pos = AnswerCodeCodec.instance.getSuccessAnswerCodeLength(pid),
		        j = 0; pos < bytes.length; pos += 2, j++) {
			final int decimal = Decimals.twoBytesToDecimal(bytes, pos);
			decimalHandler.handle(j, decimal);
		}
	}

	@Override
	public boolean isAnswerCodeSuccess(byte[] expected) {
		if (expected.length == 4) {
			return expected[0] == bytes[0] &&
			        expected[1] == bytes[1] &&
			        expected[2] == bytes[2] &&
			        expected[3] == bytes[3];

		} else if (expected.length == 6) {
			return expected[0] == bytes[0] &&
			        expected[1] == bytes[1] &&
			        expected[2] == bytes[2] &&
			        expected[3] == bytes[3] &&
			        expected[4] == bytes[4] &&
			        expected[5] == bytes[5];
		} else {
			return Arrays.equals(expected, 0, expected.length, bytes,
			        0, expected.length);
		}
	}

	private boolean isEmpty(byte[] bytes) {
		return bytes == null ||
		        bytes.length == 0 ||
		        ((bytes[0] == 'n') && (bytes[1] == 'o'));
	}

	private boolean isError(byte[] bytes) {
		return bytes == null || bytes.length == 0 ||
		        ((bytes[0] == 'S') && (bytes[1] == 'T') && (bytes[2] == 'O') && (bytes[3] == 'P')) ||
		        ((bytes[0] == 'E') && (bytes[1] == 'R') && (bytes[2] == 'R') && (bytes[3] == 'O')) ||
		        ((bytes[0] == 'U') && (bytes[1] == 'N') && (bytes[2] == 'A') && (bytes[3] == 'B')) ||
		        ((bytes[0] == 'B') && (bytes[1] == 'U') && (bytes[2] == 'S') && (bytes[3] == 'I')) ||
		        ((bytes[0] == 'C') && (bytes[1] == 'A') && (bytes[2] == 'N') && (bytes[3] == 'E'));
	}
}
