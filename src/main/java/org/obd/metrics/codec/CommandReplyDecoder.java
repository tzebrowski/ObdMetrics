package org.obd.metrics.codec;

import org.obd.metrics.pid.PidDefinition;

public class CommandReplyDecoder {
	protected static final int SUCCCESS_CODE = 40;
	
	public String getPredictedAnswerCode(final String mode) {
		return String.valueOf(SUCCCESS_CODE + Integer.parseInt(mode));
	}
	
	public String getAnswerCode(String rawData) {
		if (rawData.length() > 0) {
			final char firstChar = rawData.charAt(0);
			if (Character.isDigit(firstChar)) {
				final int answerLength = Character.getNumericValue(firstChar);
				if (rawData.length() > answerLength) {
					return rawData.substring(0, answerLength).toLowerCase();
				}
			}
		}
		return null;
	}

	public boolean isSuccessAnswerCode(PidDefinition pidDefinition, String raw) {
		// success code = 0x40 + mode + pid
		return raw.toLowerCase().startsWith(getPredictedAnswerCode(pidDefinition));
	}

	public String getPredictedAnswerCode(PidDefinition pidDefinition) {
		// success code = 0x40 + mode + pid
		return (String.valueOf(SUCCCESS_CODE + Integer.valueOf(pidDefinition.getMode())) + pidDefinition.getPid())
				.toLowerCase();
	}

	public String getRawAnswerData(PidDefinition pidDefinition, String raw) {
		// success code = 0x40 + mode + pid
		return raw.substring(getPredictedAnswerCode(pidDefinition).length());
	}

	public Long getDecimalAnswerData(PidDefinition pidDefinition, String raw) {
		// success code = 0x40 + mode + pid
		return Long.parseLong(getRawAnswerData(pidDefinition, raw), 16);
	}
}
