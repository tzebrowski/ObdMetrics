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
		this.message = Characters.normalize(in.toString());
		this.isEmpty = message == null || message.contains("nodata");
		this.isError = ERRORS.contains(message);
	}

	@Override
	public boolean isAnswerCodeSuccess(byte[] expectedSuccessAnswerCode) {
		final byte[] messageBytes = message.getBytes();

		if (expectedSuccessAnswerCode.length > messageBytes.length) {
			return false;
		} else {
			if (expectedSuccessAnswerCode.length == 4) {
				return expectedSuccessAnswerCode[0] == messageBytes[0] &&
				        expectedSuccessAnswerCode[1] == messageBytes[1] &&
				        expectedSuccessAnswerCode[2] == messageBytes[2] &&
				        expectedSuccessAnswerCode[3] == messageBytes[3];

			} else if (expectedSuccessAnswerCode.length == 6) {
				return expectedSuccessAnswerCode[0] == messageBytes[0] &&
				        expectedSuccessAnswerCode[1] == messageBytes[1] &&
				        expectedSuccessAnswerCode[2] == messageBytes[2] &&
				        expectedSuccessAnswerCode[3] == messageBytes[3] &&
				        expectedSuccessAnswerCode[4] == messageBytes[4] &&
				        expectedSuccessAnswerCode[5] == messageBytes[5];
			} else {
				return Arrays.equals(expectedSuccessAnswerCode, 0, expectedSuccessAnswerCode.length, messageBytes,
				        0, expectedSuccessAnswerCode.length);
			}
		}
	}
}
