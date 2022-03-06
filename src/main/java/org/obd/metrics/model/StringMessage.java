package org.obd.metrics.model;

import java.util.Arrays;
import java.util.List;

import org.obd.metrics.connection.Characters;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(of = "message")
final class StringMessage implements RawMessage {

	private static final List<String> ERRORS = Arrays.asList("UNABLETOCONNECT", "STOPPED", "ERROR", "CANERROR",
	        "BUSINIT");

	@Getter
	private final boolean isError;

	@Getter
	private final boolean isEmpty;

	@Getter
	private final String message;

	StringMessage(String in) {
		this.isEmpty = in == null || in.contains("nodata");
		this.message = Characters.normalize(in);
		this.isError = ERRORS.contains(message);
	}

	@Override
	public boolean isAnswerCodeSuccess(byte[] expected) {
		final byte[] messageBytes = message.getBytes();

		if (expected.length > messageBytes.length) {
			return false;
		} else {
			if (expected.length == 4) {
				return expected[0] == messageBytes[0] &&
				        expected[1] == messageBytes[1] &&
				        expected[2] == messageBytes[2] &&
				        expected[3] == messageBytes[3];

			} else if (expected.length == 6) {
				return expected[0] == messageBytes[0] &&
				        expected[1] == messageBytes[1] &&
				        expected[2] == messageBytes[2] &&
				        expected[3] == messageBytes[3] &&
				        expected[4] == messageBytes[4] &&
				        expected[5] == messageBytes[5];
			} else {
				return Arrays.equals(expected, 0, expected.length, messageBytes,
				        0, expected.length);
			}
		}
	}
}
