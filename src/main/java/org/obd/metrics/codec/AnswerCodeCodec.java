package org.obd.metrics.codec;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinition.CommandType;

public class AnswerCodeCodec {

	protected static final int SUCCCESS_CODE = 40;
	protected Map<PidDefinition, String> stringCache = new HashMap<>();
	protected Map<PidDefinition, byte[]> bytesCache = new HashMap<>();

	public String getPredictedAnswerCode(final String mode) {
		return String.valueOf(SUCCCESS_CODE + Integer.parseInt(mode));
	}

	public boolean isAnswerCodeSuccess(PidDefinition pidDefinition, String message) {
		if (CommandType.OBD.equals(pidDefinition.getCommandType())) {
			// success code = 0x40 + mode + pid
			final byte[] expectedSuccessAnswerCode = getSuccessAnswerCodeInternal(pidDefinition);
			final byte[] messageBytes = message.getBytes();
			
			if (expectedSuccessAnswerCode.length > messageBytes.length) {
				return false;
			}else {
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
					return Arrays.equals(expectedSuccessAnswerCode, 0, expectedSuccessAnswerCode.length, messageBytes, 0, expectedSuccessAnswerCode.length);
				}
			}
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

	public String getRawAnswerData(PidDefinition pidDefinition, String raw) {
		// success code = 0x40 + mode + pid
		return raw.substring(getSuccessAnswerCode(pidDefinition).length());
	}

	public Long getDecimalAnswerData(PidDefinition pidDefinition, String raw) {
		// success code = 0x40 + mode + pid
		return Long.parseLong(getRawAnswerData(pidDefinition, raw), 16);
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
}
