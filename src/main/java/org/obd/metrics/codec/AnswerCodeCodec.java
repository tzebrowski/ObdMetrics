package org.obd.metrics.codec;

import java.util.HashMap;
import java.util.Map;

import org.obd.metrics.pid.CommandType;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorMessage;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class AnswerCodeCodec {

	protected static final int SUCCCESS_CODE = 40;
	protected final Map<PidDefinition, String> stringCache = new HashMap<>();
	protected final Map<PidDefinition, byte[]> bytesCache = new HashMap<>();
	private final boolean cacheEnabled;

	public String getPredictedAnswerCode(final String mode) {
		return String.valueOf(SUCCCESS_CODE + Integer.parseInt(mode));
	}

	public boolean isAnswerCodeSuccess(final PidDefinition pidDefinition, final ConnectorMessage raw) {
		if (CommandType.OBD.equals(pidDefinition.getCommandType())) {
			// success code = 0x40 + mode + pid
			return raw.isAnswerCodeSuccess(getSuccessAnswerCodeInternal(pidDefinition));
		} else {
			return true;
		}
	}

	public int getSuccessAnswerCodeLength(final PidDefinition pidDefinition) {
		return getSuccessAnswerCode(pidDefinition).length();
	}

	public String getSuccessAnswerCode(final PidDefinition pidDefinition) {
		if (cacheEnabled && stringCache.containsKey(pidDefinition)) {
			return stringCache.get(pidDefinition);
		} else {
			final String code = generateAnswerCode(pidDefinition);
			if (cacheEnabled) {
				stringCache.put(pidDefinition, code);
			}
			return code;
		}
	}

	public Long getDecimalAnswerData(final PidDefinition pidDefinition, final ConnectorMessage raw) {
		// success code = 0x40 + mode + pid
		String rawAnswerData = getRawAnswerData(pidDefinition, raw.getMessage());
		
		if (rawAnswerData.length() > 15) {
			rawAnswerData = rawAnswerData.substring(0, 15);
		}
		
		return Long.parseLong(rawAnswerData, 16);
	}

	private String generateAnswerCode(final PidDefinition pidDefinition) {
		if (CommandType.OBD.equals(pidDefinition.getCommandType())) {
			// success code = 0x40 + mode + pid
			return (String.valueOf(SUCCCESS_CODE + Integer.valueOf(pidDefinition.getMode())) + pidDefinition.getPid())
					.toUpperCase();
		} else {
			return (pidDefinition.getMode() + pidDefinition.getPid()).toUpperCase();
		}
	}

	private byte[] getSuccessAnswerCodeInternal(final PidDefinition pidDefinition) {
		if (cacheEnabled && bytesCache.containsKey(pidDefinition)) {
			return bytesCache.get(pidDefinition);
		} else {
			final byte[] code = generateAnswerCode(pidDefinition).getBytes();
			if (cacheEnabled) {
				bytesCache.put(pidDefinition, code);
			}
			return code;
		}
	}

	private String getRawAnswerData(final PidDefinition pidDefinition, final String raw) {
		// success code = 0x40 + mode + pid
		return raw.substring(getSuccessAnswerCode(pidDefinition).length());
	}
}
