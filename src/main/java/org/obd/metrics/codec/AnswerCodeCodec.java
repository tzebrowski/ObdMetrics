package org.obd.metrics.codec;

import java.util.HashMap;
import java.util.Map;

import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinition.CommandType;
import org.obd.metrics.raw.RawMessage;

public class AnswerCodeCodec {

	protected static final int SUCCCESS_CODE = 40;
	protected Map<PidDefinition, String> stringCache = new HashMap<>();
	protected Map<PidDefinition, byte[]> bytesCache = new HashMap<>();

	public String getPredictedAnswerCode(final String mode) {
		return String.valueOf(SUCCCESS_CODE + Integer.parseInt(mode));
	}

	public boolean isAnswerCodeSuccess(PidDefinition pidDefinition, RawMessage raw) {
		if (CommandType.OBD.equals(pidDefinition.getCommandType())) {
			// success code = 0x40 + mode + pid
			return raw.isAnswerCodeSuccess(getSuccessAnswerCodeInternal(pidDefinition));
		} else {
			return true;
		}
	}

	public int getSuccessAnswerCodeLength(PidDefinition pidDefinition) {
		return getSuccessAnswerCode(pidDefinition).length();
	}

	public String getSuccessAnswerCode(PidDefinition pidDefinition) {
		if (stringCache.containsKey(pidDefinition)) {
			return stringCache.get(pidDefinition);
		} else {
			final String code = generateAnswerCode(pidDefinition);
			stringCache.put(pidDefinition, code);
			return code;
		}
	}


	public Long getDecimalAnswerData(PidDefinition pidDefinition, RawMessage raw) {
		// success code = 0x40 + mode + pid
		return Long.parseLong(getRawAnswerData(pidDefinition, raw.getMessage()), 16);
	}

	private String generateAnswerCode(PidDefinition pidDefinition) {
		if (CommandType.OBD.equals(pidDefinition.getCommandType())) {
			// success code = 0x40 + mode + pid
			return (String.valueOf(SUCCCESS_CODE + Integer.valueOf(pidDefinition.getMode())) + pidDefinition.getPid())
			        .toUpperCase();
		} else {
			return (pidDefinition.getMode() + pidDefinition.getPid()).toUpperCase();
		}
	}

	private byte[] getSuccessAnswerCodeInternal(PidDefinition pidDefinition) {
		if (bytesCache.containsKey(pidDefinition)) {
			return bytesCache.get(pidDefinition);
		} else {
			final byte[] code = generateAnswerCode(pidDefinition).getBytes();
			bytesCache.put(pidDefinition, code);
			return code;
		}
	}
	private String getRawAnswerData(PidDefinition pidDefinition, String raw) {
		// success code = 0x40 + mode + pid
		return raw.substring(getSuccessAnswerCode(pidDefinition).length());
	}

}
