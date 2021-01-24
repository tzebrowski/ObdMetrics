package org.obd.metrics.command.obd;

import java.util.ArrayList;
import java.util.List;

import org.obd.metrics.codec.Codec;
import org.obd.metrics.codec.MetricsDecoder;
import org.obd.metrics.pid.PidDefinition;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SupportedPidsCommand extends ObdCommand implements Codec<List<String>> {

	public SupportedPidsCommand(String pid) {
		super(new PidDefinition(0, "", "01", pid, "", "Supported PIDs", "", ""));
	}

	@Override
	public List<String> decode(@NonNull String data) {
		final MetricsDecoder decoder = new MetricsDecoder();
		final List<String> supportedPids = new ArrayList<String>();
		if (decoder.isSuccessAnswerCode(pid, data)) {
			final String binStr = Long.toBinaryString(decoder.getDecimalAnswerData(pid, data));

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
			log.debug("Failed to transform data: {}", data);
		}
		return supportedPids;
	}
}
