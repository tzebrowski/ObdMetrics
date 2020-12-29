package org.openobd2.core.codec;

import org.openobd2.core.pid.PidDefinition;

public class CommandReplyDecoder {
	private static final int SUCCCESS_CODE = 40;

	public String getAnswerCode(String rawData) {
		final int answerLength = Integer.valueOf((String) rawData.subSequence(0, 1));
		if (rawData.length() > answerLength) {
			return rawData.substring(0, answerLength).toLowerCase();
		} else {
			return null;
		}
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
