package org.obd.metrics.codec;

import java.util.HashMap;
import java.util.Map;

import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinition.CommandType;

public class AnswerCodeDecoder {
	protected static final int SUCCCESS_CODE = 40;
	protected Map<PidDefinition,String> answerCodeCache = new HashMap<>();
	
	public String getPredictedAnswerCode(final String mode) {
		return String.valueOf(SUCCCESS_CODE + Integer.parseInt(mode));
	}

	public boolean isAnswerCodeSuccess(PidDefinition pidDefinition, String raw) {
		if (CommandType.OBD.equals(pidDefinition.getCommandType())) {
			// success code = 0x40 + mode + pid
			return raw.toLowerCase().startsWith(getPredictedAnswerCode(pidDefinition));
		} else {
			return true;
		}
	}

	public String getPredictedAnswerCode(PidDefinition pidDefinition) {
		if (answerCodeCache.containsKey(pidDefinition)) {
			return answerCodeCache.get(pidDefinition);
		}else {
			final String code =  generateAnswerCode(pidDefinition);
			answerCodeCache.put(pidDefinition, code);
			return code;
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
	
	private String generateAnswerCode(PidDefinition pidDefinition) {
		if (CommandType.OBD.equals(pidDefinition.getCommandType())) {
			// success code = 0x40 + mode + pid
			return (String.valueOf(SUCCCESS_CODE + Integer.valueOf(pidDefinition.getMode())) + pidDefinition.getPid())
			        .toLowerCase();
		} else {
			return (pidDefinition.getMode() + pidDefinition.getPid()).toLowerCase();
		}
	}
}
