package org.obd.metrics.transport.message;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.obd.metrics.codec.AnswerCodeCodec;
import org.obd.metrics.codec.Decimals;
import org.obd.metrics.pid.PidDefinition;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(of = "message")
final class BytesMessage implements ConnectorMessage {

	private String message;

	@Getter
	private final byte[] bytes = new byte[96];
	private int length;

	BytesMessage() {
		reset();
		length = bytes.length;
	}

	@Override
	public byte[] copy() {
		return Arrays.copyOf(bytes, length);
	}
	
	void update(byte[] in, int from, int to) {
		reset();
		System.arraycopy(in, from, bytes, 0, to);
		this.length = to - from;
	}

	@Override
	public String getMessage() {
		if (message == null && bytes != null) {
			message = new String(copy(), StandardCharsets.ISO_8859_1);
		}
		return message;
	}
	
	@Override
	public int getLength() {
		return length;
	}
	
	@Override
	public void exctractDecimals(final PidDefinition pid, final DecimalReceiver decimalHandler) {
		for (int pos = new AnswerCodeCodec(false).getSuccessAnswerCodeLength(pid),
				j = 0; pos < length; pos += 2, j++) {
			final int decimal = Decimals.twoBytesToDecimal(bytes, pos);
			decimalHandler.receive(j, decimal);
		}
	}

	@Override
	public boolean isAnswerCodeSuccess(final byte[] expected) {
		if (expected.length == 4) {
			return expected[0] == bytes[0] && expected[1] == bytes[1] && expected[2] == bytes[2]
					&& expected[3] == bytes[3];

		} else if (expected.length == 6) {
			return expected[0] == bytes[0] && expected[1] == bytes[1] && expected[2] == bytes[2]
					&& expected[3] == bytes[3] && expected[4] == bytes[4] && expected[5] == bytes[5];
		} else {
			return Arrays.equals(expected, 0, expected.length, bytes, 0, expected.length);
		}
	}

	public boolean isEmpty() {
		return bytes == null || length == 0
				|| ((bytes[0] == 'N') && (bytes[1] == 'O') && (bytes[2] == 'D') && (bytes[3] == 'A'));
	}

	public boolean isError() {
		return bytes == null || length == 0
				|| (length >= 3 && (bytes[0] == 'S') && (bytes[1] == 'T') && (bytes[2] == 'O')
						&& (bytes[3] == 'P'))
				|| (length >= 3 && (bytes[0] == 'E') && (bytes[1] == 'R') && (bytes[2] == 'R')
						&& (bytes[3] == 'O'))
				|| (length >= 3 && (bytes[0] == 'U') && (bytes[1] == 'N') && (bytes[2] == 'A')
						&& (bytes[3] == 'B'))
				|| (length >= 3 && (bytes[0] == 'B') && (bytes[1] == 'U') && (bytes[2] == 'S')
						&& (bytes[3] == 'I'))
				|| (length >= 3 && (bytes[0] == 'C') && (bytes[1] == 'A') && (bytes[2] == 'N')
						&& (bytes[3] == 'E'));
	}


	private void reset() {
		Arrays.fill(bytes, 0, bytes.length, (byte) 0);
		message = null;
	}
}
