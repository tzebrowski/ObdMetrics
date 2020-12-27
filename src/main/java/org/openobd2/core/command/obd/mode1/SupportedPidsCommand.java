package org.openobd2.core.command.obd.mode1;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SupportedPidsCommand extends Mode1Command<List<String>> {

	public SupportedPidsCommand(String pid) {
		super(pid, "Get supported pids");
	}

	@Override
	public List<String> convert(@NonNull String data) {

		final List<String> supportedPids = new ArrayList<String>();
		if (isSuccessAnswerCode(data)) {
			final String binStr = Long.toBinaryString(getDecimalAnswerData(data));
			
			for (int idx = 0; idx < binStr.length(); idx++) {
				if ('1' == binStr.charAt(idx)) {
					String hexString = Integer.toHexString((idx + 1));
					if (hexString.length() == 1) {
						hexString = "0" + hexString;
					}
					supportedPids.add(hexString);
				}
			}
		} else {
			log.warn("Failed to transform data: {}", data);
		}
		return supportedPids;
	}

}
