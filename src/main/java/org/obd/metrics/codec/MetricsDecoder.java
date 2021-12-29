package org.obd.metrics.codec;

import org.obd.metrics.pid.PidDefinition;

public class MetricsDecoder {
	protected static final int SUCCCESS_CODE = 40;

	public String getPredictedAnswerCode(final String mode) {
		return String.valueOf(SUCCCESS_CODE + Integer.parseInt(mode));
	}

	public boolean isSuccessAnswerCode(PidDefinition pidDefinition, String raw) {
		final boolean isNumeric = isModeNumeric(pidDefinition);
		if (isNumeric) {
			// success code = 0x40 + mode + pid
			return raw.toLowerCase().startsWith(getPredictedAnswerCode(pidDefinition));
		} else {
			return true;
		}
	}

	public String getPredictedAnswerCode(PidDefinition pidDefinition) {
		final boolean isNumeric = isModeNumeric(pidDefinition);
		if (isNumeric) {
			// success code = 0x40 + mode + pid
			return (String.valueOf(SUCCCESS_CODE + Integer.valueOf(pidDefinition.getMode())) + pidDefinition.getPid())
			        .toLowerCase();
		} else {
			return (pidDefinition.getMode() + pidDefinition.getPid()).toLowerCase();
		}
	}

	public String getRawAnswerData(PidDefinition pidDefinition, String raw) {
		// success code = 0x40 + mode + pid
		return raw.substring(getPredictedAnswerCode(pidDefinition).length());
	}

	public Long getDecimalAnswerData(PidDefinition pidDefinition, String raw) {
		// success code = 0x40 + mode + pid
		return Long.parseLong(getRawAnswerData(pidDefinition, raw), 16);
	}

	public boolean isModeNumeric(PidDefinition pidDefinition) {
		return pidDefinition.getMode().chars().allMatch(Character::isDigit);
	}
}
