package org.obd.metrics.transport.message;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.obd.metrics.transport.Connector;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(of = "message")
final class BytesConnectorResponse implements ConnectorResponse {

	private String message;

	@Getter
	private final byte[] bytes;
	private int length;
	
	
	BytesConnectorResponse() {
		this(Connector.BUFFER_SIZE);
	}

	BytesConnectorResponse(int size) {
		bytes = new byte[size];
		length = bytes.length;
		reset();
	}

	@Override
	public String getMessage() {
		if (message == null && bytes != null) {
			message = new String(Arrays.copyOf(bytes, length), StandardCharsets.ISO_8859_1);
		}
		return message;
	}
	
	@Override
	public int getLength() {
		return length;
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

	@Override
	public boolean isEmpty() {
		return bytes == null || length == 0
				|| ((bytes[0] == 'N') && (bytes[1] == 'O') && (bytes[2] == 'D') && (bytes[3] == 'A'));
	}
	
	@Override
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
	
	void update(byte[] in, int from, int to) {
		reset();
		System.arraycopy(in, from, bytes, 0, to);
		this.length = to - from;
	}
	
	private void reset() {
		Arrays.fill(bytes, 0, bytes.length, (byte) 0);
		message = null;
	}
}
