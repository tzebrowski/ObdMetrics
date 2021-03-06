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
		super(new PidDefinition(100001l, 0, "", "01", pid, "", "Supported PIDs", 0, 0, PidDefinition.Type.DOUBLE));
	}

	@Override
	public List<String> decode(PidDefinition pid, @NonNull String data) {
		var decoder = new MetricsDecoder();
		var supportedPids = new ArrayList<String>();
		if (decoder.isSuccessAnswerCode(pid, data)) {
			var binStr = Long.toBinaryString(decoder.getDecimalAnswerData(pid, data));

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
